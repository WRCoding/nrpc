package top.ink.nrpccore.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

/**
 * desc: nrpc请求实体
 *
 * @author ink
 * date:2022-05-14 09:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RpcRequest extends RpcMessage {


    /** 服务名 */
    private String serviceName;
    /** 方法名 */
    private String methodName;
    /** 参数类型 */
    private Class<?>[] parameterTypes;
    /** 参数值 */
    private Object[] parameterValues;


}
