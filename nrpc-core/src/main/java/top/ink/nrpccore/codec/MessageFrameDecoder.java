package top.ink.nrpccore.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.stereotype.Component;

/**
 * desc: decoder
 * @author ink
 * date:2022-02-28 22:20
 */
@Component
public class MessageFrameDecoder extends LengthFieldBasedFrameDecoder {

    public MessageFrameDecoder(){
        this(1024,1,4,0,0);
    }

    public MessageFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
