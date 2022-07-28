package top.ink.nrpccore.registry;

import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.route.RouteHandle;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.net.UnknownHostException;

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
     * @param instance
     * @return void
     * Author: ink
     * Date: 2022/5/29
    */
    void registerService(String serviceName, Object instance) throws UnknownHostException;

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
     * @param serviceName
     * @return java.lang.String
     * Author: ink
     * Date: 2022/5/29
    */
    String findServiceAddress(String serviceName);

    /**
     * Description: 寻找新的地址
     * @param serviceName
     * @param inActiveAddress
     * @return java.lang.String
     * Author: ink
     * Date: 2022/7/28
    */
    String getNewServiceAddress(String serviceName, String inActiveAddress);

}
