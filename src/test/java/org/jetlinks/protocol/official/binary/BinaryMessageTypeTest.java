package org.jetlinks.protocol.official.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.AcknowledgeDeviceMessage;
import org.jetlinks.core.message.DeviceKeepaliveMessage;
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
        message.setMessageId("1");
        message.addHeader(BinaryDeviceOnlineMessage.loginToken, "admin");

        doTest(message);
    }

    @Test
    public void testKeepalive() {
        DeviceKeepaliveMessage message = new DeviceKeepaliveMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        doTest(message);
    }

    @Test
    public void testAck() {
        AcknowledgeDeviceMessage message = new AcknowledgeDeviceMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("10000");
        //message.setCode("ok");
        //message.setSuccess(true);
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

        //wrapper(data);
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testReportBuild() {
        String key = "temp";
        String value = "32.88";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x03) // 消息类型
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(1) //OBJECT对象数量
                .writeShort(key.getBytes().length)
                .writeBytes(key.getBytes())
                .writeByte(0x0B) //value的类型
                .writeShort(value.getBytes().length)
                .writeBytes(value.getBytes())
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        //wrapper(data);
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
        message.setMessageId("1");
        message.setProperties(Collections.singletonMap("temp", "36.88"));

        doTest(message);
    }

    @Test
    public void testEvent() {
        EventMessage message = new EventMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        message.setEvent("alarm");
        message.setData(Collections.singletonMap("value", 2));

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
        data.writeBytes("\r\n".getBytes());

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes())
                .writeBytes(data);
        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testEventBuild() {
        String event = "alarm";
        String key = "value";
        String value = "2";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x0a) // 消息类型 EVENT
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(event.getBytes().length) //event id length
                .writeBytes(event.getBytes())
                .writeShort(1) // OBJECT对象数量
                .writeShort(key.getBytes().length)
                .writeBytes(key.getBytes())
                .writeByte(0x0B) //value的类型
                .writeShort(value.getBytes().length)
                .writeBytes(value.getBytes())
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        //wrapper(data);
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testRead() {
        // 下发：平台 -> 设备
        ReadPropertyMessage message = new ReadPropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("test123");
        message.setProperties(Collections.singletonList("temp"));
        doTest(message);

        // 上报：设备 -> 平台
        ReadPropertyMessageReply reply = new ReadPropertyMessageReply();
        reply.setDeviceId(deviceId);
        reply.setMessageId("test123");
        reply.setProperties(Collections.singletonMap("temp", 32.88));
        doTest(reply);
    }

    @Test
    public void testWrite() {
        // 下发：平台 -> 设备
        WritePropertyMessage message = new WritePropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("test123");
        message.setProperties(Collections.singletonMap("temp", 36.88));
        doTest(message);

        // 上报：设备 -> 平台
        WritePropertyMessageReply reply = new WritePropertyMessageReply();
        reply.setDeviceId(deviceId);
        reply.setMessageId("test123");
        reply.setProperties(Collections.singletonMap("temp", 36.88));
        doTest(reply);

    }

    @Test
    public void testFunction() {
        // 下发：平台 -> 设备
        FunctionInvokeMessage message = new FunctionInvokeMessage();
        message.setFunctionId("lock");
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        message.addInput("value", 1);
        doTest(message);

        // 上报：设备 -> 平台
        FunctionInvokeMessageReply reply = new FunctionInvokeMessageReply();
        reply.setFunctionId("lock");
        reply.setDeviceId(deviceId);
        reply.setMessageId("1");
        reply.setOutput("success");
        doTest(reply);
    }

    public void doTest(DeviceMessage message) {

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
        data.writeBytes("\r\n".getBytes());

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes())
                .writeBytes(data);
        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
        //将长度字节读取截断后，直接解析报文正文
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