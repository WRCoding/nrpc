package top.ink.nrpccore.processor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import top.ink.nrpccore.anno.RpcCall;
import top.ink.nrpccore.anno.RpcService;
import top.ink.nrpccore.netty.client.Client;
import top.ink.nrpccore.netty.server.Server;
import top.ink.nrpccore.proxy.RpcProxy;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.spi.ExtensionLoad;
import top.ink.nrpccore.util.PropertiesUtil;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * desc: nrpc处理类
 *
 * @author ink
 * date:2022-05-12 22:47
 */
@Slf4j
public class RpcProcessor implements BeanPostProcessor{


    @Resource
    private ServiceRegister serviceRegister;

    @Value("${server.port}")
    private Integer port;

    private final Client client;

    private boolean start = false;

    public RpcProcessor() {
        this.client = new Client(serviceRegister);
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            startService();
            String serviceName = bean.getClass().getAnnotation(RpcService.class).ServiceName();
            serviceRegister.registerService(serviceName, bean);
        }
        return bean;
    }

    private void startService() {
        if (!start){
            Server.start(port + 1000);
            start = true;
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcCall.class)){
                RpcCall rpcCall = field.getAnnotation(RpcCall.class);
                String proxyType = PropertiesUtil.getValue("n-rpc.proxy");
                RpcProxy rpcProxy = ExtensionLoad.getExtensionLoader(RpcProxy.class).getExtension(proxyType,
                        new Object[]{client, rpcCall.ServiceName()},
                        Client.class, String.class);
                Object proxy = rpcProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
