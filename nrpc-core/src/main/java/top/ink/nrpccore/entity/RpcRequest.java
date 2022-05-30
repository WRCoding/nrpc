package top.ink.nrpccore.entity;

import lombok.Data;

import java.util.Arrays;

/**
 * desc: nrpc请求实体
 *
 * @author ink
 * date:2022-05-14 09:40
 */
@Data
public class RpcRequest {

    private byte type = 0;
    /** 序列号 */
    private String nid;
    /** 服务名 */
    private String serviceName;
    /** 方法名 */
    private String methodName;
    /** 参数类型 */
    private Class<?>[] parameterTypes;
    /** 参数值 */
    private Object[] parameterValues;

    public RpcRequest(String nid, String serviceName, String methodName, Class<?>[] parameterTypes, Object[] parameterValues) {
        this.nid = nid;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameterValues = parameterValues;
    }

    @Override
    public String toString() {
        return "NrpcRequest{" +
                "nid='" + nid + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameterValues=" + Arrays.toString(parameterValues) +
                '}';
    }
}
