package top.ink.nrpccore.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * desc: nrpc响应实体
 *
 * @author ink
 * date:2022-05-14 09:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class RpcResponse extends RpcMessage{

    private Object returnValue;

    private Exception exception;
}
