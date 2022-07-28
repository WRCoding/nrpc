package top.ink.nrpcserver.service;

import lombok.extern.slf4j.Slf4j;
import top.ink.api.callapi.HelloServiceInterface;
import top.ink.nrpccore.anno.NService;

/**
 * desc: service
 *
 * @author ink
 * date:2022-05-12 23:11
 */
@NService(ServiceName = "HelloServiceInterface")
@Slf4j
public class HelloService implements HelloServiceInterface {



    @Override
    public String say(String elem) {
        log.info("elem is {}",elem);
        return "say: " + elem;
    }
}
