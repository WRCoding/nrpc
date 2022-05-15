package top.ink.nrpcserver.service;

import top.ink.api.callapi.HelloServiceInterface;
import top.ink.nrpccore.anno.NService;

/**
 * desc: service
 *
 * @author ink
 * date:2022-05-12 23:11
 */
@NService(ServiceName = "HelloServiceInterface")
public class HelloService implements HelloServiceInterface {



    @Override
    public String say(String elem) {
        return "say: " + elem;
    }
}
