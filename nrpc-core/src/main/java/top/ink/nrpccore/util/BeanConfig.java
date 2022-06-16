package top.ink.nrpccore.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ink.nrpccore.constant.RouteType;
import top.ink.nrpccore.constant.ServiceRegisterType;
import top.ink.nrpccore.entity.RpcProperties;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.registry.impl.ZkServiceRegister;
import top.ink.nrpccore.route.RandomRouteHandle;
import top.ink.nrpccore.route.RouteHandle;
import top.ink.nrpccore.spi.ExtensionLoad;

import javax.annotation.Resource;
import java.util.ServiceLoader;

/**
 * desc: BeanConfig
 *
 * @author ink
 * date:2022-05-29 19:13
 */
@Configuration
@Slf4j
public class BeanConfig {

    @Resource
    RpcProperties properties;


    @Bean(value = "ServiceRegister")
    public ServiceRegister getServiceRegister(){
        String serviceRegisterType = properties.getServiceRegister();
        return ExtensionLoad.getExtensionLoader(ServiceRegister.class)
                .getExtension(serviceRegisterType);
    }


    @Bean(value = "RouteHandle")
    public RouteHandle getRouteHandle(){
        String routeType = properties.getRoute();
        return ExtensionLoad.getExtensionLoader(RouteHandle.class)
                .getExtension(routeType);
    }

}
