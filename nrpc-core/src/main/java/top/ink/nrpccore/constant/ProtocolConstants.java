package top.ink.nrpccore.constant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * desc: 协议相关常量
 *
 * @author ink
 * date:2022-05-30 21:54
 */
public class ProtocolConstants {

    public static final byte[] MAGIC_NUM = new byte[]{'n', 'r', 'p', 'c'};
    public static final byte VERSION = 1;
    public static final byte HEAD_LEN = 16;
    public static final byte RPC_REQUEST = 1;
    public static final byte RPC_RESPONSE = 2;
    public static final byte PING = 3;
    public static final byte PONG = 4;
    public static final int MAX_FRAME = 10 * 1024 * 1024;

    public static final String PING_STR = "ping";
    public static final String PONG_STR = "pong";

    public static AtomicInteger SEQ_ID = new AtomicInteger();

    public static Integer getSeqId(){
        return SEQ_ID.incrementAndGet();
    }

}
