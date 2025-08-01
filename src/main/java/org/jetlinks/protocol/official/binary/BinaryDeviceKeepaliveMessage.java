package org.jetlinks.protocol.official.binary;

import io.netty.buffer.ByteBuf;
import org.jetlinks.core.message.DeviceKeepaliveMessage;

public class BinaryDeviceKeepaliveMessage implements BinaryMessage<DeviceKeepaliveMessage> {

    private DeviceKeepaliveMessage message;

    @Override
    public BinaryMessageType getType() {
        return BinaryMessageType.keepalive;
    }

    @Override
    public void read(ByteBuf buf) {
        message = new DeviceKeepaliveMessage();
    }

    @Override
    public void write(ByteBuf buf) {

    }

    @Override
    public void setMessage(DeviceKeepaliveMessage message) {
        this.message = message;
    }

    @Override
    public DeviceKeepaliveMessage getMessage() {
        return message;
    }
}
