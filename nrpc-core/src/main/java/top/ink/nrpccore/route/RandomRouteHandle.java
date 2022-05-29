package top.ink.nrpccore.route;


import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * desc: 随机获取服务器节点
 *
 * @author ink
 * date:2022-04-05 10:24
 */
@Slf4j
public class RandomRouteHandle implements RouteHandle{

    public RandomRouteHandle() {
        log.info("--RandomRouteHandle--");
    }

    @Override
    public String routeServe(List<String> allService) throws RuntimeException {
        if (allService.size() == 0){
            throw new RuntimeException("没有可用的服务器");
        }
        int index = ThreadLocalRandom.current().nextInt(allService.size());
        String service = allService.get(index);
        return service;
    }
}
