package org.jetlinks.protocol.official.binary;

import io.netty.buffer.ByteBuf;
import org.jetlinks.core.message.HeaderKey;
import org.jetlinks.core.message.state.DeviceStateCheckMessage;

public class BinaryStateCheckMessage implements BinaryMessage<DeviceStateCheckMessage> {

    public static final HeaderKey<String> loginToken = HeaderKey.of("token", null);

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.stateCheck;
    }

    private DeviceStateCheckMessage message;

    @Override
    public void read(ByteBuf buf) {
        message = new DeviceStateCheckMessage();
        message.addHeader(loginToken, (String) DataType.STRING.read(buf));
    }

    @Override
    public void write(ByteBuf buf) {
        DataType.STRING.write(buf, message.getHeader(loginToken).orElse(""));
    }

    @Override
    public void setMessage(DeviceStateCheckMessage message) {
        this.message = message;
    }

    @Override
    public DeviceStateCheckMessage getMessage() {
        return message;
    }
}
