package org.jetlinks.protocol.official.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClientOptions;
import org.jetlinks.core.message.DeviceKeepaliveMessage;
import org.jetlinks.protocol.official.binary.BinaryMessageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceKeepalive {

    private final static String deviceId = "1946042226889179136";

    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        AtomicInteger messageId = new AtomicInteger(1);
        Flux.interval(Duration.ofSeconds(10))
                .flatMap(i -> Mono
                                .create(sink -> {
                                    NetClientOptions conf = new NetClientOptions().setTcpKeepAlive(true);
                                    conf.setLocalAddress("0.0.0.0");
                                    vertx.createNetClient(conf)
                                            .connect(8802, "localhost")
                                            .onFailure(err -> {
                                                System.out.println(err.getMessage());
                                                sink.success();
                                            })
                                            .onSuccess(socket -> {
                                                socket
                                                        .closeHandler((s) -> {
                                                            System.out.println("tcp-off-" + i + ":" + socket.localAddress() + " closed");
                                                            sink.success();
                                                        })
                                                        .exceptionHandler(er -> {
                                                            System.out.println("tcp-off-" + i + ":" + socket.localAddress() + " " + er.getMessage());
                                                            sink.success();
                                                        });

                                                DeviceKeepaliveMessage message = new DeviceKeepaliveMessage();
                                                message.setDeviceId(deviceId);
                                                message.setMessageId("" + messageId.getAndIncrement());
                                                message.addHeader("token", "admin");
//                                DeviceOnlineMessage message = new DeviceOnlineMessage();
//                                message.setDeviceId(deviceId);
//                                message.setMessageId("" + messageId.getAndIncrement());
//                                message.addHeader(BinaryDeviceOnlineMessage.loginToken, "admin");

                                                ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
                                                data.writeBytes("\r\n".getBytes());
                                                //ByteBuf buf = TcpDeviceMessageCodec.wrapByteByf(data);

                                                ByteBuf buf = Unpooled.buffer()
                                                        .writeInt(data.readableBytes())
                                                        .writeBytes(data);
                                                System.out.println(ByteBufUtil.prettyHexDump(buf));
                                                System.out.println(ByteBufUtil.hexDump(buf));

                                                //ByteBuf buf = TcpDeviceMessageCodec.wrapByteByf(data);
                                                //System.out.println(ByteBufUtil.prettyHexDump(buf));
                                                //System.out.println(ByteBufUtil.hexDump(buf));

                                                socket.write(Buffer.buffer(buf.array()));
                                            });
                                }),
                        1)
                .retry(3)
                .count()
                .subscribe(System.out::println);

        Thread.sleep(3600000);
    }
}
