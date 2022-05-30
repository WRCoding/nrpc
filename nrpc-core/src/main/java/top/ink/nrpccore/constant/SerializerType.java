package top.ink.nrpccore.constant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * desc: 序列化类型
 *
 * @author ink
 * date:2022-05-30 22:24
 */
@AllArgsConstructor
@NoArgsConstructor
public enum SerializerType {

    /** JSON */
    JSON((byte)0x00, "json");


    private byte flag;
    private String name;

    public byte getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

    public static String getName(byte flag){
        for (SerializerType serializerType : SerializerType.values()) {
            if (serializerType.getFlag() == flag){
                return serializerType.getName();
            }
        }
        throw new UnsupportedOperationException("不支持该flag: " + flag);
    }

}
