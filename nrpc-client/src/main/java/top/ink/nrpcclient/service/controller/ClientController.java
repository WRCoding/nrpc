package top.ink.nrpcclient.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.ink.api.callapi.HelloServiceInterface;
import top.ink.api.callapi.User;
import top.ink.api.callapi.UserServiceInterface;
import top.ink.nrpccore.anno.RpcCall;

/**
 * desc: Client
 *
 * @author ink
 * date:2022-05-15 15:24
 */
@RestController
public class ClientController {

    @RpcCall(ServiceName = "HelloServiceInterface")
    public HelloServiceInterface helloServiceInterface;

    @RpcCall(ServiceName = "UserServiceInterface")
    public UserServiceInterface userServiceInterface;

    @GetMapping("/a")
    public String a(String name){
        return helloServiceInterface.say(name);
    }

    @PostMapping("/user")
    public String user(@RequestBody User user){
        return userServiceInterface.addUser(user);
    }
}
