package top.ink.nrpccore.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import top.ink.nrpccore.entity.RpcProperties;
import top.ink.nrpccore.registry.CuratorUtils;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.route.RouteHandle;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc: zk注册中心
 *
 * @author ink
 * date:2022-05-29 19:51
 */

@Slf4j
public class ZkServiceRegister implements ServiceRegister {

    private static final Map<String ,Object> SERVICE_INSTANCE_MAP = new ConcurrentHashMap<>();

    @Resource
    private RpcProperties rpcProperties;

    @Resource
    private RouteHandle routeHandle;

    @Value("${server.port}")
    private Integer port;

    @Override
    public void registerService(String serviceName, Object instance) throws UnknownHostException {

        if (!SERVICE_INSTANCE_MAP.containsKey(serviceName)){
            String path = createPath(serviceName);
            CuratorFramework zkClient = CuratorUtils.getZkClient(rpcProperties);
            CuratorUtils.createEphemeralNode(zkClient, path);
            SERVICE_INSTANCE_MAP.put(serviceName, instance);
        }
    }

    private String createPath(String serviceName) throws UnknownHostException {
        String address = InetAddress.getLocalHost().getHostAddress();

        return CuratorUtils.ZK_REGISTER_ROOT_PATH +
                CuratorUtils.LINE_SEPARATOR + serviceName + CuratorUtils.LINE_SEPARATOR + address
                + ":" + (port+10000);
    }

    @Override
    public Object getServiceInstance(String serviceName) {
        return SERVICE_INSTANCE_MAP.get(serviceName);
    }


    @Override
    public String findServiceAddress(String serviceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient(rpcProperties);
        List<String> serviceAddressList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
        return routeHandle.routeServe(serviceAddressList);
    }
}
