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


    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T) PROXY_MAP.computeIfAbsent(clazz, (elem -> Proxy.newProxyInstance(elem.getClassLoader(),
                new Class[]{elem},
                this)));
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .serviceName(serviceName)
                .parameterTypes(method.getParameterTypes())
                .parameterValues(args)
                .rpcId(RPC_ID.getAndIncrement()).build();
        return client.sendRequest(rpcRequest);
    }

}
