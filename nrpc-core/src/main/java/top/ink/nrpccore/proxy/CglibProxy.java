package top.ink.nrpccore.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.netty.client.Client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 林北
 * @description
 * @date 2022-06-16 16:43
 */
@Slf4j
public class CglibProxy implements RpcProxy, MethodInterceptor {


    /** 缓存proxy对象,提升性能 */
    protected static final Map<Class<?>, Object> PROXY_MAP = new ConcurrentHashMap<>();

    private final Client client;
    private final String serviceName;

    private static final AtomicInteger RPC_ID = new AtomicInteger(10000);


    public CglibProxy(Client client, String serviceName) {
        this.client = client;
        this.serviceName = serviceName;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return  (T) PROXY_MAP.computeIfAbsent(clazz, (this::createProxy));
    }

    @Override
    public Object createProxy(Class<?> clazz){
        // 创建动态代理增强类
        Enhancer enhancer = new Enhancer();
        // 设置类加载器
        enhancer.setClassLoader(clazz.getClassLoader());
        // 设置被代理类
        enhancer.setSuperclass(clazz);
        // 设置方法拦截器
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .methodName(method.getName())
                .serviceName(serviceName)
                .parameterTypes(method.getParameterTypes())
                .parameterValues(objects)
                .rpcId(RPC_ID.getAndIncrement()).build();
        return client.sendRequest(rpcRequest);
    }
}
