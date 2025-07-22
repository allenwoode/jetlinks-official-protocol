package org.jetlinks.protocol.official.binary;

import org.jetlinks.core.message.CommonDeviceMessage;
import org.jetlinks.core.message.MessageType;

public class KeepaliveMessage extends CommonDeviceMessage<KeepaliveMessage> {

    @Override
    public MessageType getMessageType() {
        return MessageType.LOG;
    }
}
