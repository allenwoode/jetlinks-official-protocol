package org.jetlinks.protocol.official.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.core.message.*;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.PasswordType;
import org.jetlinks.protocol.official.binary.AckCode;
import org.jetlinks.protocol.official.binary.BinaryAcknowledgeDeviceMessage;
import org.jetlinks.protocol.official.binary.BinaryDeviceOnlineMessage;
import org.jetlinks.protocol.official.binary.BinaryMessageType;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
public class TcpDeviceMessageCodec implements DeviceMessageCodec {

    public static final String CONFIG_KEY_SECURE_KEY = "secureKey";

    public static final DefaultConfigMetadata tcpConfig = new DefaultConfigMetadata("TCP认证配置", "")
            .add(CONFIG_KEY_SECURE_KEY, "secureKey", "密钥", new PasswordType());


    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.TCP;
    }

    /**
     * 协议对消息上下文进行解码
     *
     * @param context 消息上下文
     * @return
     */
    @NonNull
    @Override
    public Publisher<? extends Message> decode(@NonNull MessageDecodeContext context) {

        ByteBuf payload = context.getMessage().getPayload();
        log.debug(">>>>>接收设备TCP报文: {}", ByteBufUtil.hexDump(payload));

        // read index
        payload.readInt();

        //ByteBuf buf = wrapByteByf(payload);
        //DeviceMessage message = BinaryMessageType.read(buf);
        //log.debug(">>>>>设备上行TCP消息: {}", message);

        //处理tcp连接后的首次消息
        if (context.getDevice() == null) {
            return handleLogin(payload, context);
        }

        DeviceMessage message = BinaryMessageType.read(payload, context.getDevice().getDeviceId());
        log.debug(">>>>>接收设备TCP消息: {}", message);

        return Mono.justOrEmpty(message);
    }

    private Mono<DeviceMessage> handleLogin(ByteBuf payload, MessageDecodeContext context) {
        DeviceMessage message = BinaryMessageType.read(payload);
        log.debug(">>>>>首次认证TCP消息: {}", message);

        if (message instanceof DeviceOnlineMessage) {
            String token = message.getHeader(BinaryDeviceOnlineMessage.loginToken).orElse(null);
            String deviceId = message.getDeviceId();

            return context
                    .getDevice(deviceId)
                    .flatMap(device -> device
                            .getConfig(CONFIG_KEY_SECURE_KEY)
                            .flatMap(config -> {
                                if (Objects.equals(config.asString(), token)) {
                                    // 设备认证通过
                                    return ack(message, AckCode.ok, context).thenReturn(message);
                                }
                                return Mono.empty();
                            }))
                    .switchIfEmpty(Mono.defer(() -> ack(message, AckCode.noAuth, context)));

        } else {
            return ack(message, AckCode.noAuth, context);
        }
    }

    public static ByteBuf wrapByteByf(ByteBuf payload) {
        return Unpooled.wrappedBuffer(
                Unpooled.buffer().writeInt(payload.writerIndex()),
                payload);
    }

    private <T> Mono<T> ack(DeviceMessage source, AckCode code, MessageDecodeContext context) {
        log.debug(">>>>>ack回应: {}-{}", source, code);
        if (source == null) {
            return Mono.empty();
        }

        AcknowledgeDeviceMessage message = new AcknowledgeDeviceMessage();
        message.addHeader(BinaryAcknowledgeDeviceMessage.codeHeader, code.name());
        message.setDeviceId(source.getDeviceId());
        message.setMessageId(source.getMessageId());
        message.setCode(code.name());
        message.setSuccess(code == AckCode.ok);

        source.getHeader(BinaryMessageType.HEADER_MSG_SEQ)
                .ifPresent(seq -> message.addHeader(BinaryMessageType.HEADER_MSG_SEQ, seq));

        return ((FromDeviceMessageContext) context)
                .getSession()
                .send(EncodedMessage.simple(
                        wrapByteByf(BinaryMessageType.write(message, Unpooled.buffer()))
                ))
                .then(Mono.fromRunnable(() -> {
                    if (source instanceof DeviceOnlineMessage && code != AckCode.ok) {
                        // 认证失败关闭session连接
                        ((FromDeviceMessageContext) context).getSession().close();
                    }
                }));
    }

    /**
     * 协议对消息上下文进行编码
     * @param context 消息上下文
     * @return
     */
    @NonNull
    @Override
    public Publisher<? extends EncodedMessage> encode(@NonNull MessageEncodeContext context) {
        DeviceMessage deviceMessage = ((DeviceMessage) context.getMessage());
        log.debug(">>>>>平台发送TCP消息: {}", deviceMessage);

        if (deviceMessage instanceof DisconnectDeviceMessage) {
            return Mono.empty();
        }

        ByteBuf buf = BinaryMessageType.write(deviceMessage, Unpooled.buffer());
        log.debug(">>>>>平台发送TCP报文: {}", ByteBufUtil.hexDump(buf));

        return Mono.just(EncodedMessage.simple(
                wrapByteByf(buf)
        ));
    }


}
