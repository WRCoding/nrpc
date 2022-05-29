package top.ink.nrpccore.route;


import java.util.List;

/**
 * desc: 获取服务器节点方法
 *
 * @author ink
 * date:2022-04-05 10:08
 */
public interface RouteHandle {


    /**
     * Description: 从所有服务器节点中选一个出来
     * @param allService
     * @return top.ink.dimgateway.entity.service.ServiceInfo
     * Author: ink
     * Date: 2022/4/5
    */
    String routeServe(List<String> allService);
}
