package top.ink.nrpccore.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.registry.CuratorUtils;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.route.RouteHandle;

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
    private RouteHandle routeHandle;



    @Override
    public void registerService(String serviceName, Object instance) throws UnknownHostException {
        String address = InetAddress.getLocalHost().getHostAddress();
        if (!SERVICE_INSTANCE_MAP.containsKey(serviceName)){
            String path = CuratorUtils.ZK_REGISTER_ROOT_PATH +
                    CuratorUtils.LINE_SEPARATOR + serviceName + CuratorUtils.LINE_SEPARATOR + address;
            CuratorFramework zkClient = CuratorUtils.getZkClient();
            CuratorUtils.createPersistentNode(zkClient, path);
            SERVICE_INSTANCE_MAP.put(serviceName, instance);
        }
    }

    @Override
    public Object getServiceInstance(String serviceName) {
        return SERVICE_INSTANCE_MAP.get(serviceName);
    }

    @Override
    public String findServiceAddress(String serviceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceAddressList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
        return getRouteHandle().routeServe(serviceAddressList);
    }
}
