package top.ink.nrpccore.handle;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.entity.RpcResponse;
import top.ink.nrpccore.proxy.RpcProxy;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * desc: NrpcResponseHandle
 *
 * @author ink
 * date:2022-05-15 09:20
 */
@Slf4j
@ChannelHandler.Sharable
public class NrpcResponseHandle extends SimpleChannelInboundHandler<RpcResponse> {

    public static final Map<String, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String serviceName = RpcProxy.CHANNEL_MAP_SERVICE_NAME.get(channel);
        RpcProxy.CHANNEL_MAP_SERVICE_NAME.remove(channel);
        RpcProxy.SERVICE_NAME_MAP_CHANNEL.remove(serviceName);
        log.info("断开连接了!");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        long start = System.currentTimeMillis();
        scheduledExecutorService.scheduleAtFixedRate(new Task(serviceName, start, scheduledExecutorService,(newChannel -> {
            log.info("newChannel id: {}", newChannel.id().asShortText());
            RpcProxy.CHANNEL_MAP_SERVICE_NAME.put(newChannel, serviceName);
            RpcProxy.SERVICE_NAME_MAP_CHANNEL.put(serviceName,newChannel);
            stopTask(scheduledExecutorService);
        })), 1, 3, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }


    class Task implements Runnable {

        String serviceName;
        ScheduledExecutorService scheduledExecutorService;
        Long start;
        Consumer<Channel> consumer;

        public Task(String serviceName, Long start, ScheduledExecutorService scheduledExecutorService, Consumer<Channel> consumer) {
            this.serviceName = serviceName;
            this.start = start;
            this.scheduledExecutorService = scheduledExecutorService;
            this.consumer = consumer;
        }


        @Override
        public void run() {
            log.info("spend: {}", System.currentTimeMillis() - start);
            if (System.currentTimeMillis() - start >= 20000) {
                stopTask(scheduledExecutorService);
            } else {
                log.info("---正在重连---");
                try {
                    Channel channel = RpcProxy.initChannel(serviceName);
                    if (channel != null){
                        consumer.accept(channel);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }


    }

    private void stopTask(ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.shutdown();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        log.info("nrpcResponse: {}", rpcResponse);
        String nid = rpcResponse.getNid();
        Promise<Object> promise = PROMISE_MAP.get(nid);
        if (promise != null) {
            Object returnValue = rpcResponse.getReturnValue();
            Exception exception = rpcResponse.getException();
            if (exception != null) {
                promise.setFailure(exception);
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
