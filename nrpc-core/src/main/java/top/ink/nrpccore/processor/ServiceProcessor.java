package top.ink.nrpccore.processor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.anno.NService;
import top.ink.nrpccore.netty.MessageFrameDecoder;
import top.ink.nrpccore.netty.RpcCodec;
import top.ink.nrpccore.entity.RpcProperties;
import top.ink.nrpccore.handle.NrpcRequestHandle;
import top.ink.nrpccore.netty.server.Server;
import top.ink.nrpccore.registry.ServiceRegister;

import javax.annotation.Resource;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * desc: nrpc服务处理类
 *
 * @author ink
 * date:2022-05-12 22:47
 */
@Component
@Slf4j
public class ServiceProcessor implements ApplicationContextAware, InitializingBean {


    @Resource
    private ServiceRegister serviceRegister;

    @Value("${server.port}")
    private Integer port;

    private static final String PREFIX = "/";

    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> nServiceMap = applicationContext.getBeansWithAnnotation(NService.class);
        if (nServiceMap.size() > 0) {
            Server.start(port + 10000);
            nServiceMap.forEach((key, value) -> {
                try {
                    String serviceName = value.getClass().getAnnotation(NService.class).ServiceName();
                    serviceRegister.registerService(serviceName, value);
                } catch (UnknownHostException e) {
                    log.error("registerService error: {}", e.getMessage());
                }
            });
        }
    }

    @Override
    public void afterPropertiesSet() {

    }
}
