package top.ink.nrpccore.entity;

import lombok.Builder;
import lombok.Data;

/**
 * desc: Rpc协议
 *
 * @author ink
 * date:2022-05-30 21:41
 */
@Data
@Builder
public class RpcProtocol {
    private byte[] magicNum;
    private byte version;
    private byte msgType;
    private byte serializerType;
    private int seqId;
    private Object data;
}
