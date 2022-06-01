package top.ink.nrpccore.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * desc: nrpc响应实体
 *
 * @author ink
 * date:2022-05-14 09:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RpcResponse extends RpcMessage{

    private Object returnValue;

    private Exception exception;
}
