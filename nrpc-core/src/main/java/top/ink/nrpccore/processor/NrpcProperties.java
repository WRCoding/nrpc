package top.ink.nrpccore.processor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * desc: NrpcServerProperties
 *
 * @author ink
 * date:2022-05-13 21:23
 */

@ConfigurationProperties(prefix = "nrpc")
@Data
@Component
public class NrpcProperties {

    private String zkHost;

    private Integer zkConnectTimeout;

    private String serverHost;

    private Integer serverPort;


}
