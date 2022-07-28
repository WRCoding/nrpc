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
import top.ink.nrpccore.util.PropertiesUtil;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

    protected static ScheduledFuture<?> scheduledFuture;

    public Client(ServiceRegister serviceRegister) {
        bootstrap = new Bootstrap();
        ClientHandle clientHandle = new ClientHandle(this);
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
                                .addLast(clientHandle);
                    }
                });
        this.serviceRegister = serviceRegister;
    }

    @SneakyThrows
    private Channel connect(String address) {
        log.info("address: {}", address);
        CompletableFuture<Channel> cf = new CompletableFuture<>();
        String[] ipAndPort = address.split(":");
        InetSocketAddress socketAddress = new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        bootstrap.connect(socketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("client connected success address: {}", address);
                cf.complete(future.channel());
            } else {
//                throw new IllegalStateException();
                cf.complete(null);
            }
        });
        return cf.get();
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest) {
        String value = PropertiesUtil.getValue("n-rpc.serializer.type");
        log.info("n-rpc.serializer.type: {}", value);
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
            promise.await(3, TimeUnit.SECONDS);
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
                (s -> {
                    String serviceAddress = serviceRegister.findServiceAddress(serviceName);
                    log.info("client connect [{}]", serviceAddress);
                    return connect(serviceAddress);
                }));
        CHANNEL_MAP_SERVICE.put(channel, serviceName);
        return channel;
    }

    public void reconnect(Channel channel) {
        SocketAddress socketAddress = channel.remoteAddress();
        String inActiveAddress = socketAddress.toString().substring(1);
        log.info("inActive: {}",inActiveAddress);
        String serviceName = CHANNEL_MAP_SERVICE.remove(channel);
        CHANNEL_MAP.remove(serviceName);
        Long start = System.currentTimeMillis();
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new ReConnectTask(start, serviceName, inActiveAddress), 1, 5, TimeUnit.SECONDS);
    }

    class ReConnectTask implements Runnable {

        private final Long start;
        private final String serviceName;

        private final String inActiveAddress;

        public ReConnectTask(Long start, String serviceName, String inActiveAddress) {
            this.start = start;
            this.serviceName = serviceName;
            this.inActiveAddress = inActiveAddress;
        }

        @Override
        public void run() {
            log.info("reconnecting...");
            if (System.currentTimeMillis() - start > RECONNECT_TIMEOUT) {
                log.error("reconnect fail");
                scheduledFuture.cancel(true);
            } else {
                Channel newChannel = connect(serviceRegister.findServiceAddress(serviceName));
                if (newChannel != null) {
                    log.info("reconnect success");
                    CHANNEL_MAP_SERVICE.put(newChannel, serviceName);
                    CHANNEL_MAP.put(serviceName, newChannel);
                    scheduledFuture.cancel(true);
                }
            }
        }

        private void stopTask() {
            scheduledExecutorService.shutdownNow();
        }
    }


}
