package top.ink.nrpccore.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.constant.ProtocolConstants;
import top.ink.nrpccore.constant.SerializerType;
import top.ink.nrpccore.entity.RpcProtocol;
import top.ink.nrpccore.entity.RpcRequest;
import top.ink.nrpccore.entity.RpcResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * desc: NrpcCodec
 *
 * @author ink
 * date:2022-05-14 23:21
 */
@Slf4j
public class RpcCodec extends ByteToMessageCodec<RpcProtocol> {

    private static final AtomicInteger RPC_ID = new AtomicInteger();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol rpcProtocol, ByteBuf byteBuf) {
        byte msgType = rpcProtocol.getMsgType();
        byte serializerType = rpcProtocol.getSerializerType();

        byteBuf.writeBytes(rpcProtocol.getMagicNum());
        byteBuf.writeByte(rpcProtocol.getVersion());
        byteBuf.writeByte(7);
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        byteBuf.writeByte(msgType);
        byteBuf.writeByte(serializerType);
        byteBuf.writeInt(RPC_ID.getAndIncrement());

        byte[] dataBytes = null;
        int length = ProtocolConstants.HEAD_LEN;
        if (msgType != ProtocolConstants.PING && msgType != ProtocolConstants.PONG){
            Serializer serializer = getSerializer(serializerType);
            dataBytes = serializer.serialize(rpcProtocol.getData());
            length += dataBytes.length;
        }

        if (dataBytes != null){
            byteBuf.writeBytes(dataBytes);
        }
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(writerIndex - length + ProtocolConstants.MAGIC_NUM.length + 2);
        byteBuf.writeInt(length);
        byteBuf.writerIndex(writerIndex);
    }

    private Serializer getSerializer(byte serializerType) {
        return new RpcJsonSerializer();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        checkMagicNumAndVersion(byteBuf);
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

    private void checkMagicNumAndVersion(ByteBuf byteBuf) {
        byte[] temp = new byte[ProtocolConstants.MAGIC_NUM.length];
        byteBuf.readBytes(temp);
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] != ProtocolConstants.MAGIC_NUM[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(temp));
            }
        }
        byte version = byteBuf.readByte();
        if (version != ProtocolConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }
}
