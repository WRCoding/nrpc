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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.anno.NService;
import top.ink.nrpccore.codec.MessageFrameDecoder;
import top.ink.nrpccore.codec.RpcCodec;
import top.ink.nrpccore.entity.RpcProperties;
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
    private RpcProperties rpcProperties;

    private ZkClient zkClient;

    private static final String PREFIX = "/";

    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> nServiceMap = applicationContext.getBeansWithAnnotation(NService.class);
        if (nServiceMap.size() > 0) {
            start();
            zkClient = (ZkClient) applicationContext.getBean("zkClient");
            nServiceMap.forEach((key, value) -> {
                registerService(value);
                log.info("key:{},value:{}", key, value.getClass().getAnnotation(NService.class).ServiceName());
            });
        }
    }

    private void registerService(Object value){
        String serviceName = value.getClass().getAnnotation(NService.class).ServiceName();
        String rootPath = PREFIX + serviceName;
        if (!zkClient.exists(rootPath)) {
            zkClient.createPersistent(rootPath);
        }
        String path = rootPath + PREFIX + rpcProperties.getServerHost() + ":" + rpcProperties.getServerPort();
        zkClient.createEphemeral(path);
        SERVICE_MAP.put(serviceName, value);
    }

    private void start(){
        try {
            NioEventLoopGroup boss = new NioEventLoopGroup(1);
            NioEventLoopGroup worker = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new MessageFrameDecoder())
                                    .addLast(new RpcCodec())
                                    .addLast(new NrpcRequestHandle(SERVICE_MAP));
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(rpcProperties.getServerPort()).sync();
            future.channel().closeFuture().addListener(elem -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("netty 启动失败: {}", e.getMessage());
        }
    }


    @Override
    public void afterPropertiesSet() {
        log.info("nrpc服务启动成功");
    }
}
