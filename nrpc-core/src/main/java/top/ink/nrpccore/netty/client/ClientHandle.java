package top.ink.nrpccore.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.constant.ProtocolConstants;
import top.ink.nrpccore.constant.SerializerType;
import top.ink.nrpccore.entity.RpcMessage;
import top.ink.nrpccore.entity.RpcNull;
import top.ink.nrpccore.entity.RpcProtocol;
import top.ink.nrpccore.entity.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 林北
 * @description
 * @date 2022-05-31 19:13
 */
@Slf4j
public class ClientHandle extends SimpleChannelInboundHandler<RpcMessage> {

    public static Map<Integer, Promise<Object>> PROMISE_MAP = new ConcurrentHashMap<>();


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {
        if (rpcMessage.getMsgType() == ProtocolConstants.RPC_RESPONSE) {
            RpcResponse rpcResponse = (RpcResponse) rpcMessage;
            Object value = rpcResponse.getReturnValue();
            Exception exception = rpcResponse.getException();
            Promise<Object> promise = PROMISE_MAP.get(rpcMessage.getRpcId());
            if (exception != null) {
                promise.setFailure(exception);
            } else {
                promise.setSuccess(value);
            }
        } else if (rpcMessage.getMsgType() == ProtocolConstants.PONG) {
            log.info("heartbeat [{}]", ProtocolConstants.PONG_STR);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                RpcProtocol rpcProtocol = RpcProtocol.builder()
                        .magicNum(ProtocolConstants.MAGIC_NUM)
                        .version(ProtocolConstants.VERSION)
                        .serializerType(SerializerType.JSON.getFlag())
                        .seqId(Client.SEQ_ID.incrementAndGet()).build();
                ctx.channel().writeAndFlush(rpcProtocol);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}