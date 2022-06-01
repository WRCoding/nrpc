//package top.ink.nrpccore.handle;
//
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import lombok.extern.slf4j.Slf4j;
//import top.ink.nrpccore.entity.RpcRequest;
//import top.ink.nrpccore.entity.RpcResponse;
//
//import java.lang.reflect.Method;
//import java.util.Map;
//
///**
// * desc: NrpcRequestHandle
// *
// * @author ink
// * date:2022-05-15 08:57
// */
//@Slf4j
//@ChannelHandler.Sharable
//public class NrpcRequestHandle extends SimpleChannelInboundHandler<RpcRequest> {
//
//    private Map<String, Object> serviceMap;
//
//    public NrpcRequestHandle(Map<String, Object> serviceMap) {
//        this.serviceMap = serviceMap;
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest nrpcRequest) throws Exception {
//        RpcResponse rpcResponse = new RpcResponse();
//        rpcResponse.setNid(nrpcRequest.getNid());
//        try {
//            log.info("nrpcRequest: {}", nrpcRequest);
//            String serviceName = nrpcRequest.getServiceName();
//            String methodName = nrpcRequest.getMethodName();
//            Object object = serviceMap.get(serviceName);
//            Method method = object.getClass().getMethod(methodName, nrpcRequest.getParameterTypes());
//            Object invoke = method.invoke(object, nrpcRequest.getParameterValues());
//            log.info("invoke: {}", invoke);
//            rpcResponse.setReturnValue(invoke);
//        } catch (Exception e) {
//            log.error("invoke error: {}", e);
//            rpcResponse.setException(new Exception(e.getCause().getMessage()));
//        }
//        ctx.channel().writeAndFlush(rpcResponse);
//    }
//}
