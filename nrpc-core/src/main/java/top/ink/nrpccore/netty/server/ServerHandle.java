package top.ink.nrpccore.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.constant.ProtocolConstants;
import top.ink.nrpccore.constant.SerializerType;
import top.ink.nrpccore.entity.RpcMessage;
import top.ink.nrpccore.entity.RpcProtocol;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.entity.RpcResponse;
import top.ink.nrpccore.registry.ServiceRegister;
import top.ink.nrpccore.util.SpringBeanFactory;

import java.lang.reflect.Method;

/**
 * @author 林北
 * @description
 * @date 2022-05-31 19:15
 */
@Slf4j
public class ServerHandle extends SimpleChannelInboundHandler<RpcMessage> {



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage) throws Exception {
        RpcProtocol rpcProtocol = RpcProtocol.builder()
                .magicNum(ProtocolConstants.MAGIC_NUM)
                .version(ProtocolConstants.VERSION)
                .serializerType(SerializerType.JSON.getFlag())
                .seqId(ProtocolConstants.getSeqId()).build();
        if (rpcMessage.getMsgType() == ProtocolConstants.PING){
            rpcProtocol.setMsgType(ProtocolConstants.PONG);
            log.info("receive heartbeat [{}]", ProtocolConstants.PING_STR);
            channelHandlerContext.channel().writeAndFlush(rpcProtocol);
        }else if (rpcMessage.getMsgType() == ProtocolConstants.RPC_REQUEST){
            RpcRequest rpcRequest = (RpcRequest) rpcMessage;
            rpcProtocol.setMsgType(ProtocolConstants.RPC_RESPONSE);
            rpcProtocol.setData(invokeMethod(rpcRequest));
            channelHandlerContext.channel().writeAndFlush(rpcProtocol);
        }
    }

    private RpcResponse invokeMethod(RpcRequest rpcRequest){
        RpcResponse rpcResponse = RpcResponse.builder()
                .rpcId(rpcRequest.getRpcId()).build();
        try {
            Object instance = getInstance(rpcRequest.getServiceName());
            Method method = instance.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(instance, rpcRequest.getParameterValues());
            rpcResponse.setReturnValue(result);
        } catch (Exception e) {
            log.error("[{}]方法执行失败,原因: {}", rpcRequest.getMethodName(), e.getCause().getMessage());
            rpcResponse.setException(new Exception(e.getCause().getMessage()));
        }
        return rpcResponse;
    }

    private Object getInstance(String serviceName) {
        ServiceRegister serviceRegister = SpringBeanFactory.getBean("ServiceRegister", ServiceRegister.class);
        return serviceRegister.getServiceInstance(serviceName);
    }
}
