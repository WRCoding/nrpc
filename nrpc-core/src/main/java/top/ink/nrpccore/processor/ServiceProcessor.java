package top.ink.nrpccore.processor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.anno.NService;
import top.ink.nrpccore.codec.MessageFrameDecoder;
import top.ink.nrpccore.codec.NrpcCodec;
import top.ink.nrpccore.handle.NrpcRequestHandle;

import javax.annotation.Resource;
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
    private NrpcProperties nrpcProperties;

    private ZkClient zkClient;

    private static final String PREFIX = "/";

    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> NServiceMap = applicationContext.getBeansWithAnnotation(NService.class);
        if (NServiceMap.size() > 0) {
            zkClient = (ZkClient) applicationContext.getBean("zkClient");
            NServiceMap.forEach((key, value) -> {
                try {
                    registerService(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("key:{},value:{}", key, value.getClass().getAnnotation(NService.class).ServiceName());
            });
        }
    }

    private void registerService(Object value) throws InterruptedException {
        String serviceName = value.getClass().getAnnotation(NService.class).ServiceName();
        String rootPath = PREFIX + serviceName;
        if (!zkClient.exists(rootPath)) {
            zkClient.createPersistent(rootPath);
        }
        String path = rootPath + PREFIX + nrpcProperties.getServerHost() + ":" + nrpcProperties.getServerPort();
        zkClient.createEphemeral(path);
        SERVICE_MAP.put(serviceName, value);
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap().group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MessageFrameDecoder());
                        ch.pipeline().addLast(new NrpcCodec());
                        ch.pipeline().addLast(new NrpcRequestHandle(SERVICE_MAP));
                    }
                });
        ChannelFuture future = serverBootstrap.bind(nrpcProperties.getServerPort()).sync();
        future.channel().closeFuture().addListener(elem -> {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        });
    }


    @Override
    public void afterPropertiesSet() {
        log.info("nrpc服务启动成功");
    }
}
