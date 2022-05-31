package top.ink.nrpccore.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import top.ink.nrpccore.entity.RpcMessage;
import top.ink.nrpccore.entity.RpcResponse;

/**
 * @author 林北
 * @description
 * @date 2022-05-31 19:15
 */
public class ServerHandle extends SimpleChannelInboundHandler<RpcMessage> {



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {

    }
}
