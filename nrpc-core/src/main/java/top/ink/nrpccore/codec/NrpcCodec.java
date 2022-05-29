package top.ink.nrpccore.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.entity.RpcResponse;

import java.util.List;

/**
 * desc: NrpcCodec
 *
 * @author ink
 * date:2022-05-14 23:21
 */
@Slf4j
public class NrpcCodec extends ByteToMessageCodec<Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf byteBuf) {
        log.info("encode: {}",msg);
        byte[] bytes = Algorithm.valueOf("json").serialize(msg);
        if (msg instanceof RpcRequest){
            byteBuf.writeByte(((RpcRequest) msg).getType());
        }else{
            byteBuf.writeByte(((RpcResponse) msg).getType());
        }
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        byte type = byteBuf.readByte();
        int len = byteBuf.readInt();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        Algorithm algorithm = Algorithm.values()[0];
        if (type == 0){
            RpcRequest nrpcRequest = algorithm.deserialize(RpcRequest.class, bytes);
            log.info("decode: {}",nrpcRequest);
            out.add(nrpcRequest);
        }else{
            RpcResponse rpcResponse = algorithm.deserialize(RpcResponse.class, bytes);
            log.info("decode: {}", rpcResponse);
            out.add(rpcResponse);
        }
    }
}
