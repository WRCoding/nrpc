package top.ink.nrpcclient.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ink.api.callapi.HelloServiceInterface;
import top.ink.nrpccore.anno.Ncall;

/**
 * desc: Client
 *
 * @author ink
 * date:2022-05-15 15:24
 */
@RestController
public class ClientController {

    @Ncall(ServiceName = "HelloServiceInterface")
    public HelloServiceInterface helloServiceInterface;

    @GetMapping("/a")
    public String a(String name){
        return helloServiceInterface.say(name);
    }
}
