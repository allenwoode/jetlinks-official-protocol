package org.jetlinks.protocol.official.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessageReply;
import org.jetlinks.core.message.property.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class BinaryMessageTypeTest {

    private final String deviceId = "1946042226889179136";

    @Test
    public void testOnline() {
        DeviceOnlineMessage message = new DeviceOnlineMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("test123");
        message.addHeader(BinaryDeviceOnlineMessage.loginToken, "admin");

//        ByteBuf byteBuf = BinaryMessageType.write(message, Unpooled.buffer());
//
//        System.out.println(ByteBufUtil.prettyHexDump(byteBuf));
//        ByteBuf buf = Unpooled
//                .buffer()
//                .writeInt(byteBuf.readableBytes())
//                .writeBytes(byteBuf);
//
//        System.out.println(ByteBufUtil.prettyHexDump(buf));
//        //登录报文
//        System.out.println(ByteBufUtil.hexDump(buf));

        doTest(message);
    }

    @Test
    public void testOnlineBuild() {
        String secureKey = "admin";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x01) // 消息类型 online: 0x01
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(secureKey.getBytes().length) // secureKey长度
                .writeBytes(secureKey.getBytes())        // secureKey 平台配置值
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        wrapper(data);
    }

    @Test
    public void testStateCheckBuild() {
        String secureKey = "admin";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x0b) // 消息类型 keepalive:0x00 online:0x01 ack:0x02
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(10000) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(secureKey.getBytes().length) // secureKey长度
                .writeBytes(secureKey.getBytes())        // secureKey 平台配置值
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testKeepaliveBuild() {
        String secureKey = "admin";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x00) // 消息类型 keepalive:0x00 online:0x01 ack:0x02
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(secureKey.getBytes().length) // secureKey长度
                .writeBytes(secureKey.getBytes())        // secureKey 平台配置值
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testReport() {
        ReportPropertyMessage message = new ReportPropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("test123");
        message.setProperties(Collections.singletonMap("temp", 37.88));

        doTest(message);
    }

    @Test
    public void testReportBuild() {
        //String secureKey = "admin";
        String key = "temp";
        String value = "32.55";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x03)
                .writeLong(System.currentTimeMillis())
                .writeShort(2)
                .writeShort(deviceId.getBytes().length)
                .writeBytes(deviceId.getBytes())
                .writeShort(1)
                .writeShort(key.getBytes().length)
                .writeBytes(key.getBytes())
                .writeByte(0x0B)
                .writeShort(value.getBytes().length)
                .writeBytes(value.getBytes())
                //.writeShort(secureKey.getBytes().length)
                //.writeBytes(secureKey.getBytes())
                .writeBytes("\r\n".getBytes());

        wrapper(data);
    }

    @Test
    public void testEvent() {
        EventMessage message = new EventMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("test123");
        message.setEvent("alarm");
        message.setData(Collections.singletonMap("value", 1));

        doTest(message);
    }

    @Test
    public void testRead() {
        ReadPropertyMessage message = new ReadPropertyMessage();
        message.setDeviceId("test");
        message.setMessageId("test123");
        message.setProperties(Collections.singletonList("temp"));
        doTest(message);

        ReadPropertyMessageReply reply = new ReadPropertyMessageReply();
        reply.setDeviceId("test");
        reply.setMessageId("test123");
        reply.setProperties(Collections.singletonMap("temp", 32.88));
        doTest(reply);

    }

    @Test
    public void testWrite() {
        WritePropertyMessage message = new WritePropertyMessage();
        message.setDeviceId("test");
        message.setMessageId("test123");
        message.setProperties(Collections.singletonMap("temp", 32.88));
        doTest(message);

        WritePropertyMessageReply reply = new WritePropertyMessageReply();
        reply.setDeviceId("test");
        reply.setMessageId("test123");
        reply.setProperties(Collections.singletonMap("temp", 32.88));
        doTest(reply);

    }

    @Test
    public void testFunction() {
        FunctionInvokeMessage message = new FunctionInvokeMessage();
        message.setFunctionId("123");
        message.setDeviceId("test");
        message.setMessageId("test123");
        message.addInput("test", 1);
        doTest(message);

        FunctionInvokeMessageReply reply = new FunctionInvokeMessageReply();
        reply.setDeviceId("test");
        reply.setMessageId("test123");
        reply.setOutput(123);
        doTest(reply);

    }

    public void doTest(DeviceMessage message) {

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
        data.writeBytes("\r\n".getBytes());

//        System.out.println(ByteBufUtil.prettyHexDump(data));
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes())
                .writeBytes(data);
        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
        //将长度字节读取后，直接解析报文正文
        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        if (null != read.getHeaders()) {
            read.getHeaders().forEach(message::addHeader);
        }

        System.out.println(read);
        Assert.assertEquals(read.toString(), message.toString());
    }

    private void wrapper(ByteBuf data) {
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        //将长度字节读取后，直接解析报文正文
        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
    }
}