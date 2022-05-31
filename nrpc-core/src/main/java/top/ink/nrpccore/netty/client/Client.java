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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 林北
 * @description
 * @date 2022-05-31 16:55
 */
@Slf4j
public class Client {
    private Bootstrap bootstrap;
    private ServiceRegister serviceRegister;
    private static Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    public static AtomicInteger SEQ_ID = new AtomicInteger();


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
                                .addLast(new ClientHandle());
                    }
                });
        serviceRegister = SpringBeanFactory.getBean("ServiceRegister",ServiceRegister.class);
    }

    @SneakyThrows
    private Channel connect(String address){
        CompletableFuture<Channel> cf = new CompletableFuture();
        String[] ipAndPort = address.split(":");
        InetSocketAddress socketAddress = new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        bootstrap.connect(socketAddress).addListener( (ChannelFutureListener) future -> {
            if (future.isSuccess()){
                log.info("client connected success address: {}", address);
                cf.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        return cf.get();
    }

    @SneakyThrows
    public Object sendRequest(RpcRequest rpcRequest){
        String serviceAddress = serviceRegister.findServiceAddress(rpcRequest);
        Channel channel = getChannel(serviceAddress);
        DefaultPromise<Object> promise;
        if (channel.isActive()){
            promise = new DefaultPromise<>(channel.eventLoop());
            RpcProtocol rpcProtocol = RpcProtocol.builder().magicNum(ProtocolConstants.MAGIC_NUM)
                    .version(ProtocolConstants.VERSION)
                    .msgType(ProtocolConstants.RPC_REQUEST)
                    .serializerType(SerializerType.JSON.getFlag())
                    .seqId(SEQ_ID.incrementAndGet())
                    .data(rpcRequest).build();
            ClientHandle.PROMISE_MAP.put(rpcRequest.getRpcId(), promise);
            channel.writeAndFlush(rpcProtocol);
            promise.await();
            if (promise.isSuccess()){
                return promise.getNow();
            }else{
                throw new Exception(promise.cause());
            }
        }else{
            throw new IllegalStateException();
        }
    }

    private Channel getChannel(String serviceAddress) {
        Channel channel = CHANNEL_MAP.get(serviceAddress);
        if (channel == null){
            channel = connect(serviceAddress);
            CHANNEL_MAP.put(serviceAddress, channel);
        }
        return channel;
    }

}
