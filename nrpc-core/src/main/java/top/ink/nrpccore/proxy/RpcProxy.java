package top.ink.nrpccore.proxy;

import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.netty.client.Client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * desc: NrpcProxy
 *
 * @author ink
 * date:2022-05-14 19:47
 */
@Slf4j
public class RpcProxy implements InvocationHandler {


    /** 缓存proxy对象,提升性能 */
    protected static final Map<Class<?>, Object> PROXY_MAP = new ConcurrentHashMap<>();

    private final Client client;
    private final String serviceName;

    private static final AtomicInteger RPC_ID = new AtomicInteger(10000);

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


//    public static Channel initChannel(String serviceName) throws InterruptedException {
//        ZkClient zkClient = SpringBeanFactory.getBean("zkClient", ZkClient.class);
//        String path = DIAGONAL + serviceName;
//        List<String> serviceList = zkClient.getChildren(path);
//        if (serviceList.size() > 0){
//            int index = ThreadLocalRandom.current().nextInt(serviceList.size());
//            String service = serviceList.get(index);
//            String[] hostAndPort = service.split(":");
//            NioEventLoopGroup work = new NioEventLoopGroup();
//            Bootstrap bootstrap = new Bootstrap().group(work)
//                    .channel(NioSocketChannel.class)
//                    .handler(new ChannelInitializer<NioSocketChannel>() {
//                        @Override
//                        protected void initChannel(NioSocketChannel ch) {
//                            ch.pipeline().addLast(new MessageFrameDecoder());
//                            ch.pipeline().addLast(new RpcCodec());
//                            ch.pipeline().addLast(new LoggingHandler());
//                            ch.pipeline().addLast(new ServerHandle());
//                        }
//                    });
//            ChannelFuture future = bootstrap.connect(hostAndPort[0], Integer.parseInt(hostAndPort[1])).sync();
//            Channel channel = future.channel();
//            channel.closeFuture().addListener(x -> {
//                work.shutdownGracefully();
//            });
//            log.info("serviceName: {}, 客户端启动: {}", serviceName, channel.id().asShortText());
//            return channel;
//        }else{
//            return null;
//        }
//    }


}
