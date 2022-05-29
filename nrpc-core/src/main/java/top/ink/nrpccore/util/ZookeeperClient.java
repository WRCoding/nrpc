package top.ink.nrpccore.util;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * desc: zk客户端
 *
 * @author ink
 * date:2022-05-13 21:04
 */
@Configuration
@Slf4j
public class ZookeeperClient {


    @Bean(name = "zkClient")
    public ZkClient zkClient(){
        return new ZkClient("server-1:2181,server-2:2181,server-3:2181",20000);
    }
}
