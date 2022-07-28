package top.ink.nrpcserver.service;

import lombok.extern.slf4j.Slf4j;
import top.ink.api.callapi.User;
import top.ink.api.callapi.UserServiceInterface;
import top.ink.nrpccore.anno.NService;

import java.util.ArrayList;
import java.util.List;

/**
 * desc: UserService
 *
 * @author ink
 * date:2022-07-28 16:04
 */
@NService(ServiceName = "UserServiceInterface")
@Slf4j
public class UserService implements UserServiceInterface {

    private List<User> list = new ArrayList<>();

    @Override
    public String addUser(User user) {
        list.add(user);
        for (User u : list) {
            log.info(u.toString());
        }
        return list.size()+"";
    }
}
