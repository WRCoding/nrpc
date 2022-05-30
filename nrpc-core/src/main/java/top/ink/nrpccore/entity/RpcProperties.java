package top.ink.nrpccore.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.constant.RouteType;
import top.ink.nrpccore.constant.ServiceRegisterType;

/**
 * desc: NrpcServerProperties
 *
 * @author ink
 * date:2022-05-13 21:23
 */

@ConfigurationProperties(prefix = "n-rpc")
@Data
@Component(value = "RpcProperties")
public class RpcProperties {

    private String zkHost;

    private Integer zkConnectTimeout = 2000;

    private Integer zkBaseSleepTimeMs = 1000;

    private Integer zkRetry = 3;

    private String route = RouteType.RANDOM.name();

    private String serviceRegister = ServiceRegisterType.ZOOKEEPER.name();
}
