package top.ink.nrpccore.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.stereotype.Component;
import top.ink.nrpccore.constant.ProtocolConstants;

/**
 * desc: decoder
 * @author ink
 * date:2022-02-28 22:20
 */
@Component
public class MessageFrameDecoder extends LengthFieldBasedFrameDecoder {

    public MessageFrameDecoder(){
        this(ProtocolConstants.MAX_FRAME,6,4,-10,0);
    }

    public MessageFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
