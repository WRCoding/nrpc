package top.ink.nrpccore.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import top.ink.nrpccore.anno.NCall;
import top.ink.nrpccore.netty.MessageFrameDecoder;
import top.ink.nrpccore.netty.RpcCodec;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.handle.NrpcResponseHandle;
import top.ink.nrpccore.netty.client.Client;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * desc: NrpcProxy
 *
 * @author ink
 * date:2022-05-14 19:47
 */
@Slf4j
public class RpcProxy implements InvocationHandler {


    private static final String DIAGONAL = "/";

    public static final Map<String, Channel> SERVICE_NAME_MAP_CHANNEL = new ConcurrentHashMap<>();

    /** 反存Channel和Service的关系,方便重连 */
    public static final Map<Channel, String> CHANNEL_MAP_SERVICE_NAME = new ConcurrentHashMap<>();

    /** 缓存proxy对象,提升性能 */
    public static final Map<Class<?>, Object> PROXY_MAP = new ConcurrentHashMap<>();

    private Client client;
    private String serviceName;

    private static AtomicInteger RPC_ID = new AtomicInteger(10000);

    public RpcProxy(Client client, String serviceName) {
        this.client = client;
        this.serviceName = serviceName;
    }

    public <T> T getProxy(Class<T> clazz){
        if (!PROXY_MAP.containsKey(clazz)){
            T proxy = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
            PROXY_MAP.put(clazz, proxy);
        }
        return (T) PROXY_MAP.get(clazz);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .serviceName(serviceName)
                .parameterTypes(method.getParameterTypes())
                .parameterValues(args)
                .rpcId(RPC_ID.getAndIncrement()).build();
        return client.sendRequest(rpcRequest);
    }

    public static Object getProxy(Field field) throws ClassNotFoundException {
        return null;
//        String className = field.getType().getName();
//        if (PROXY_MAP.containsKey(className)){
//            return PROXY_MAP.get(className);
//        }else{
//            ClassLoader loader = Thread.currentThread().getContextClassLoader();
//            Object o = Proxy.newProxyInstance(loader, new Class[]{Class.forName(className)}, (proxy, method, args) -> {
//                String nid = UUID.randomUUID().toString().replaceAll("-", "");
//                String serviceName = field.getAnnotation(NCall.class).ServiceName();
//                RpcRequest nrpcRequest = new RpcRequest(nid, serviceName, method.getName(), method.getParameterTypes(), args);
//                Channel channel = SERVICE_NAME_MAP_CHANNEL.get(serviceName);
//                DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
//                NrpcResponseHandle.PROMISE_MAP.put(nrpcRequest.getNid(), promise);
//                channel.writeAndFlush(nrpcRequest);
//                promise.await();
//                log.info(promise.isSuccess()+"");
//                if (promise.isSuccess()){
//                    return promise.getNow();
//                }else{
//                    throw new RuntimeException(promise.cause());
//                }
//            });
//            PROXY_MAP.put(className, o);
//            return o;
//        }
    }


    public static Channel initChannel(String serviceName) throws InterruptedException {
        ZkClient zkClient = SpringBeanFactory.getBean("zkClient", ZkClient.class);
        String path = DIAGONAL + serviceName;
        List<String> serviceList = zkClient.getChildren(path);
        if (serviceList.size() > 0){
            int index = ThreadLocalRandom.current().nextInt(serviceList.size());
            String service = serviceList.get(index);
            String[] hostAndPort = service.split(":");
            NioEventLoopGroup work = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap().group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new MessageFrameDecoder());
                            ch.pipeline().addLast(new RpcCodec());
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new NrpcResponseHandle());
                        }
                    });
            ChannelFuture future = bootstrap.connect(hostAndPort[0], Integer.parseInt(hostAndPort[1])).sync();
            Channel channel = future.channel();
            channel.closeFuture().addListener(x -> {
                work.shutdownGracefully();
            });
            log.info("serviceName: {}, 客户端启动: {}", serviceName, channel.id().asShortText());
            return channel;
        }else{
            return null;
        }
    }


}
