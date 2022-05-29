package top.ink.nrpccore.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.entity.RpcProperties;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.registry.CuratorUtils;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.route.RouteHandle;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc: zk注册中心
 *
 * @author ink
 * date:2022-05-29 19:51
 */
@Component
@DependsOn(value = "SpringBeanFactory")
@Slf4j
public class ZkServiceRegister implements ServiceRegister {

    private static final Map<String ,Object> SERVICE_INSTANCE_MAP = new ConcurrentHashMap<>();
    private RouteHandle routeHandle;

    public ZkServiceRegister() {
        log.info("--ZkServiceRegister--");
        routeHandle = getRouteHandle();
    }

    private RouteHandle getRouteHandle() {
        RpcProperties rpcProperties = SpringBeanFactory.getBean("RpcProperties", RpcProperties.class);
        String route = rpcProperties.getRoute();
        log.info("route: {}", route);
        if ("custom".equals(route)){
            ServiceLoader<RouteHandle> load = ServiceLoader.load(RouteHandle.class);
            return load.iterator().next();
        }
        return null;
    }

    @Override
    public void registerService(String serviceName, String address) {
        if (!SERVICE_INSTANCE_MAP.containsKey(serviceName)){
            String path = CuratorUtils.ZK_REGISTER_ROOT_PATH +
                    CuratorUtils.LINE_SEPARATOR + serviceName + CuratorUtils.LINE_SEPARATOR + address;
            CuratorFramework zkClient = CuratorUtils.getZkClient();
            CuratorUtils.createPersistentNode(zkClient, path);
        }
    }

    @Override
    public Object getServiceInstance(String serviceName) {
        return SERVICE_INSTANCE_MAP.get(serviceName);
    }

    @Override
    public String findServiceAddress(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceAddressList = CuratorUtils.getChildrenNodes(zkClient, serviceName);

        return null;
    }
}
