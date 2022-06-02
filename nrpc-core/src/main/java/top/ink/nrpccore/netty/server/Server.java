package top.ink.nrpccore.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.netty.MessageFrameDecoder;
import top.ink.nrpccore.netty.RpcCodec;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * desc: 服务启动
 *
 * @author ink
 * date:2022-05-31 23:50
 */
@Slf4j
public class Server {


    public static void start(Integer port){
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            NioEventLoopGroup boss = new NioEventLoopGroup(1);
            NioEventLoopGroup worker = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new MessageFrameDecoder())
                                    .addLast(new RpcCodec())
                                    .addLast(new ServerHandle());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(socketAddress).sync();
            log.info("server init success [{}]", port);
            future.channel().closeFuture().addListener(elem -> {
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            });
        } catch (InterruptedException | UnknownHostException e) {
            log.error("netty 启动失败: {}", e.getMessage());
        }
    }
}
