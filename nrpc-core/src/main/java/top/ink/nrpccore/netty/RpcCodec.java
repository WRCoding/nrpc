package top.ink.nrpccore.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import top.ink.nrpccore.codec.RpcJsonSerializer;
import top.ink.nrpccore.codec.Serializer;
import top.ink.nrpccore.constant.ProtocolConstants;
import top.ink.nrpccore.constant.SerializerType;
import top.ink.nrpccore.entity.*;

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
        byteBuf.writeInt(rpcProtocol.getSeqId());

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
        int length = byteBuf.readInt();
        byte msgType = byteBuf.readByte();
        byte serializerType = byteBuf.readByte();
        int rpcId = byteBuf.readInt();
        if (msgType == ProtocolConstants.PING || msgType == ProtocolConstants.PONG){
            String data = msgType == ProtocolConstants.PING ? "ping" : "pong";
            RpcHeartBeat rpcHeartBeat = RpcHeartBeat.builder()
                    .msgType(msgType)
                    .rpcId(rpcId)
                    .data(data).build();
            out.add(rpcHeartBeat);
        }

        int dataLen = length - ProtocolConstants.HEAD_LEN;

        if (dataLen > 0){
            byte[] bytes = new byte[dataLen];
            byteBuf.readBytes(bytes);
            Serializer serializer = getSerializer(serializerType);
            if (msgType == ProtocolConstants.RPC_REQUEST){
                RpcRequest rpcRequest = serializer.deserialize(RpcRequest.class, bytes);
                out.add(rpcRequest);
            }
            if (msgType == ProtocolConstants.RPC_RESPONSE){
                RpcResponse rpcResponse = serializer.deserialize(RpcResponse.class, bytes);
                out.add(rpcResponse);
            }
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
        byte padding = byteBuf.readByte();
    }
}
