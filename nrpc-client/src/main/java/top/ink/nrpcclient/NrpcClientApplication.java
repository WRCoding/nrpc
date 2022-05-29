package top.ink.nrpcclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.ink.nrpccore.anno.EnableNRpc;

@SpringBootApplication
@EnableNRpc
public class NrpcClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(NrpcClientApplication.class, args);
    }


}
