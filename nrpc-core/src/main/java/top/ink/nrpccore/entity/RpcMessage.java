package top.ink.nrpccore.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author 林北
 * @description 通信消息基类
 * @date 2022-05-31 09:11
 */
@SuperBuilder
@Data
public class RpcMessage {

    private byte msgType;
    private Integer rpcId;
}
