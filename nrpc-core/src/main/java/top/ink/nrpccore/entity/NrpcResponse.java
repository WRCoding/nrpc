package top.ink.nrpccore.entity;

import lombok.Data;

/**
 * desc: nrpc响应实体
 *
 * @author ink
 * date:2022-05-14 09:46
 */
@Data
public class NrpcResponse {

    private byte type = 1;

    /** 序列号 */
    private String nid;

    private Object returnValue;

    private Exception exception;
}
