package top.ink.nrpccore.registry;

import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.route.RouteHandle;
import top.ink.nrpccore.util.SpringBeanFactory;

/**
 * desc: 服务注册接口
 *
 * @author ink
 * date:2022-05-29 19:45
 */
public interface ServiceRegister {

    /**
     * Description: 注册服务到注册中心
     * @param serviceName
     * @param address
     * return void
     * Author: ink
     * Date: 2022/5/29
    */
    void registerService(String serviceName, String address);

    /**
     * Description: 获取服务实例
     * @param serviceName
     * @return java.lang.Object
     * Author: ink
     * Date: 2022/5/29
    */
    Object getServiceInstance(String serviceName);

    /**
     * Description: 查找服务地址
     * @param RpcRequest
     * @return java.lang.String
     * Author: ink
     * Date: 2022/5/29
    */
    String findServiceAddress(RpcRequest RpcRequest);

    /**
     * 获取RouteHandle
     * @author longxun.wang
     * @date 2022/5/30 15:25
     * @return top.ink.nrpccore.route.RouteHandle
     */
    default RouteHandle getRouteHandle(){
        return SpringBeanFactory.getBean("RouteHandle",RouteHandle.class);
    }
}
