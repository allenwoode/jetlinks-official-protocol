package org.jetlinks.protocol.official.binary;

import io.netty.buffer.ByteBuf;
import org.jetlinks.core.message.HeaderKey;

public class BinaryKeepaliveMessage implements BinaryMessage<KeepaliveMessage> {

    public static final HeaderKey<String> loginToken = HeaderKey.of("token", null);

    private KeepaliveMessage message;

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.keepalive;
    }

    @Override
    public void read(ByteBuf buf) {
        message = new KeepaliveMessage();
        message.addHeader(loginToken, (String) DataType.STRING.read(buf));
    }

    @Override
    public void write(ByteBuf buf) {
        DataType.STRING.write(buf, message.getHeader(loginToken).orElse(""));
    }

    @Override
    public void setMessage(KeepaliveMessage message) {
        this.message = message;
    }

    @Override
    public KeepaliveMessage getMessage() {
        return message;
    }
}
