package top.ink.nrpccore.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.constant.ProtocolConstants;
import top.ink.nrpccore.constant.SerializerType;
import top.ink.nrpccore.entity.RpcProtocol;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.netty.MessageFrameDecoder;
import top.ink.nrpccore.netty.RpcCodec;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 林北
 * @description
 * @date 2022-05-31 16:55
 */
@Slf4j
public class Client {
    private final Bootstrap bootstrap;
    private final ServiceRegister serviceRegister;

    private static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private static final Integer RECONNECT_TIMEOUT = 20000;

    /**
     * 反存Channel和Service的关系,方便重连
     */
    protected static final Map<Channel, String> CHANNEL_MAP_SERVICE = new ConcurrentHashMap<>();


    protected static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);


    public Client() {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        nioSocketChannel.pipeline()
                                .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                .addLast(new MessageFrameDecoder())
                                .addLast(new RpcCodec())
                                .addLast(new ClientHandle(Client.this));
                    }
                });
        serviceRegister = SpringBeanFactory.getBean("ServiceRegister", ServiceRegister.class);
    }

    @SneakyThrows
    private Channel connect(String address) {
        CompletableFuture<Channel> cf = new CompletableFuture<>();
        String[] ipAndPort = address.split(":");
        InetSocketAddress socketAddress = new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        bootstrap.connect(socketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("client connected success address: {}", address);
                cf.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return cf.get();
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest) {
        Channel channel = getChannel(rpcRequest);
        DefaultPromise<Object> promise;
        if (channel.isActive()) {
            promise = new DefaultPromise<>(channel.eventLoop());
            RpcProtocol rpcProtocol = RpcProtocol.builder().magicNum(ProtocolConstants.MAGIC_NUM)
                    .version(ProtocolConstants.VERSION)
                    .msgType(ProtocolConstants.RPC_REQUEST)
                    .serializerType(SerializerType.JSON.getFlag())
                    .seqId(ProtocolConstants.getSeqId())
                    .data(rpcRequest).build();
            ClientHandle.PROMISE_MAP.put(rpcRequest.getRpcId(), promise);
            channel.writeAndFlush(rpcProtocol);
            promise.await();
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                throw new Exception(promise.cause());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private Channel getChannel(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        Channel channel = CHANNEL_MAP.computeIfAbsent(serviceName,
                (s -> connect(serviceRegister.findServiceAddress(serviceName))));
        CHANNEL_MAP_SERVICE.put(channel, serviceName);
        return channel;
    }

    public void reconnect(Channel channel) {
        String serviceName = CHANNEL_MAP_SERVICE.remove(channel);
        CHANNEL_MAP.remove(serviceName);
        Long start = System.currentTimeMillis();
        scheduledExecutorService.scheduleAtFixedRate(new ReConnectTask(start, serviceName), 1, 5, TimeUnit.SECONDS);
    }

    class ReConnectTask implements Runnable {

        private final Long start;
        private final String serviceName;

        public ReConnectTask(Long start, String serviceName) {
            this.start = start;
            this.serviceName = serviceName;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() - start > RECONNECT_TIMEOUT) {
                log.error("reconnect fail");
                stopTask();
            } else {
                Channel newChannel = connect(serviceRegister.findServiceAddress(serviceName));
                if (newChannel != null) {
                    log.info("reconnect success");
                    CHANNEL_MAP_SERVICE.put(newChannel, serviceName);
                    CHANNEL_MAP.put(serviceName, newChannel);
                    stopTask();
                }
            }
        }

        private void stopTask() {
            scheduledExecutorService.shutdownNow();
        }
    }


}
