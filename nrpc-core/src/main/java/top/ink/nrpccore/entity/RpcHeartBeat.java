package top.ink.nrpccore.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * @author 林北
 * @description rpc心跳
 * @date 2022-05-31 09:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RpcHeartBeat extends RpcMessage {

    private String data;
}
