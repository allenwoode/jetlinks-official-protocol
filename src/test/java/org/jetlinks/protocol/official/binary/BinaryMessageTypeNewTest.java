package org.jetlinks.protocol.official.binary;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.AcknowledgeDeviceMessage;
import org.jetlinks.core.message.DeviceKeepaliveMessage;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BinaryMessageTypeNewTest {

    private final String deviceId = "11110001";

    @Test
    public void testAck() {
        AckCode code = AckCode.unsupportedMessage;
        AcknowledgeDeviceMessage message = new AcknowledgeDeviceMessage();
        message.setDeviceId(deviceId);
        message.addHeader(BinaryAcknowledgeDeviceMessage.codeHeader, code.name());
        message.setMessageId("15");
        message.setCode(code.name());
        //doTest(message);

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
        data.writeBytes("\r\n".getBytes());

        ByteBuf buf = Unpooled.buffer()
                .writeInt(0)
                .writeBytes(data);
        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
    }

    @Test
    public void testKeepalive() {
        DeviceKeepaliveMessage message = new DeviceKeepaliveMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        doTest(message);
    }

    @Test
    public void testOnlineBuild() {
        String secureKey = "admin";
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x01) // 消息类型 online: 0x01
                .writeLong(0) // 时间戳
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

        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
    }

    @Test
    public void testKeepaliveBuild() {
        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x00) // 消息类型 online: 0x01
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        //wrapper(data);
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
    }

    @Test
    public void testReport1() {
        // CHARGE_STATE
        ReportPropertyMessage message = new ReportPropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        Map<String, Object> properties = new HashMap<>();
        message.setProperties(properties);
        Map<String, Object> chargeState = new HashMap<>();
        properties.put("CHARGE_STATE", chargeState);
        chargeState.put("num", "6");
        chargeState.put("state", "001100");

//        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
//        ByteBuf buf = Unpooled.buffer()
//                .writeInt(data.readableBytes())
//                .writeBytes(data);
//        System.out.println(ByteBufUtil.prettyHexDump(buf));
//        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testReportBuild1() {
        String propertyKey = "CHARGE_STATE";
        String numKey = "num";
        int numValue = 6;
        String stateKey = "state";
        String stateValue = "001100";

        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x03) // 消息类型 REPORT_PROPERTY: 0x03
                .writeLong(0) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(1) // 属性对象数量
                .writeShort(propertyKey.getBytes().length) // 属性key长度
                .writeBytes(propertyKey.getBytes())     // 属性key "CHARGE_STATE"
                .writeByte(0x0E) // value类型 OBJECT: 0x0E
                .writeShort(2) // OBJECT中字段数量
                // 第一个字段 "num": 6
                .writeShort(numKey.getBytes().length)   // 字段key长度
                .writeBytes(numKey.getBytes())          // 字段key "num"
                .writeByte(0x04) // value类型 INT: 0x04
                .writeInt(numValue) // int值 6
                // 第二个字段 "state": "001100"
                .writeShort(stateKey.getBytes().length) // 字段key长度
                .writeBytes(stateKey.getBytes())        // 字段key "state"
                .writeByte(0x0B) // value类型 STRING: 0x0B
                .writeShort(stateValue.getBytes().length) // 字符串长度
                .writeBytes(stateValue.getBytes())      // 字符串值 "001100"
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(0) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testReport2() {
        // LOCK_STATE
        ReportPropertyMessage message = new ReportPropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> lockState = new HashMap<>();
        properties.put("LOCK_STATE", lockState);
        lockState.put("num", 6);
        lockState.put("state", "001110");
        message.setProperties(properties);

        doTest(message);
    }

    @Test
    public void testReportBuild2() {
        String propertyKey = "LOCK_STATE";
        String numKey = "num";
        int numValue = 6;
        String stateKey = "state";
        String stateValue = "110011";

        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x03) // 消息类型 REPORT_PROPERTY: 0x03
                .writeLong(0) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(1) // 属性对象数量
                .writeShort(propertyKey.getBytes().length) // 属性key长度
                .writeBytes(propertyKey.getBytes())     // 属性key "CHARGE_STATE"
                .writeByte(0x0E) // value类型 OBJECT: 0x0E
                .writeShort(2) // OBJECT中字段数量
                // 第一个字段 "num": 6
                .writeShort(numKey.getBytes().length)   // 字段key长度
                .writeBytes(numKey.getBytes())          // 字段key "num"
                .writeByte(0x04) // value类型 INT: 0x04
                .writeInt(numValue) // int值 6
                // 第二个字段 "state": "001100"
                .writeShort(stateKey.getBytes().length) // 字段key长度
                .writeBytes(stateKey.getBytes())        // 字段key "state"
                .writeByte(0x0B) // value类型 STRING: 0x0B
                .writeShort(stateValue.getBytes().length) // 字符串长度
                .writeBytes(stateValue.getBytes())      // 字符串值 "001100"
                .writeBytes("\r\n".getBytes());          // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(0) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    @Test
    public void testReport() {
        // CHARGE_STATE
        ReportPropertyMessage message = new ReportPropertyMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> chargeState = new HashMap<>();
        chargeState.put("num", "6");
        chargeState.put("state", "001100");
        properties.put("CHARGE_STATE", chargeState);
        // LOCK_STATE
        Map<String, Object> lockState = new HashMap<>();
        properties.put("LOCK_STATE", lockState);
        lockState.put("num", "6");
        lockState.put("state", "001110");
        message.setProperties(properties);
        doTest(message);
    }

    @Test
    public void testEvent() {
        EventMessage message = new EventMessage();
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        message.setEvent("LOCK_OPEN_TYPE");
        Map<String, Object> event = new HashMap<>();
        event.put("port", 4);
        event.put("type", "3");
        message.setData(event);

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
        String event = "LOCK_OPEN_TYPE";
        String portKey = "port";
        int portValue = 5;
        String typeKey = "type";
        String typeValue = "2";

        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x0A) // 消息类型 EVENT: 0x0A
                .writeLong(0) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(event.getBytes().length) // event名称长度
                .writeBytes(event.getBytes())        // event名称 "LOCK_OPEN_TYPE"
                .writeShort(2) // OBJECT中字段数量
                // 第一个字段 "port": 4
                .writeShort(portKey.getBytes().length) // 字段key长度
                .writeBytes(portKey.getBytes())        // 字段key "port"
                .writeByte(0x04) // value类型 INT: 0x04
                .writeInt(portValue) // int值 4
                // 第二个字段 "type": "3"
                .writeShort(typeKey.getBytes().length) // 字段key长度
                .writeBytes(typeKey.getBytes())        // 字段key "type"
                .writeByte(0x0B) // value类型 STRING: 0x0B
                .writeShort(typeValue.getBytes().length) // 字符串长度
                .writeBytes(typeValue.getBytes())      // 字符串值 "3"
                .writeBytes("\r\n".getBytes());        // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(0) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
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
        message.setFunctionId("LOCK_OPEN_CMD");
        message.setDeviceId(deviceId);
        message.setMessageId("1");
        message.addInput("port", "5");
        message.addInput("type", "0");
//        doTest(message);

        // 上报：设备 -> 平台
//        FunctionInvokeMessageReply reply = new FunctionInvokeMessageReply();
//        reply.setFunctionId("LOCK_OPEN_CMD");
//        reply.setDeviceId(deviceId);
//        reply.setMessageId("1");
        //reply.setSuccess(false);
        //reply.setCode("device offline");
        //reply.setMessage("设备已离线");
        //doTest(reply);

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());
        data.writeBytes("\r\n".getBytes());

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes())
                .writeBytes(data);
        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        buf.readInt();
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);

    }

    @Test
    public void testFunctionBuild() {
        String functionId = "LOCK_OPEN_CMD";
        String key1 = "port";
        String value1 = "5";
        String key2 = "type";
        String value2 = "1";

        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x08) // 消息类型 FUNCTION_INVOKE_REPLY: 0x09
                .writeLong(System.currentTimeMillis()) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(functionId.getBytes().length)
                .writeBytes(functionId.getBytes())
                .writeShort(2) // OBJECT中字段数量
                .writeShort(key1.getBytes().length) //
                .writeBytes(key1.getBytes())        //
                .writeByte(0x0b)
                .writeShort(value1.getBytes().length)
                .writeBytes(value1.getBytes())
                .writeShort(key2.getBytes().length)
                .writeBytes(key2.getBytes())
                .writeByte(0x0b)
                .writeShort(value2.getBytes().length) //
                .writeBytes(value2.getBytes())     //
                .writeBytes("\r\n".getBytes());     // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
        System.out.println("0000003c08000001987e91f475000100083131313130303031000d4c4f434b5f4f50454e5f434d4400020004706f72740b0001350004747970650b0001300d0a");

        buf.readInt();
        //System.out.println("buf after length: " + buf.readableBytes());
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
    }

    @Test
    public void testFunctionReplyBuild() {
        String functionId = "LOCK_OPEN_CMD";
        String code = "";
        String message = "device offline";

        ByteBuf data = Unpooled
                .buffer()
                .writeByte(0x09) // 消息类型 FUNCTION_INVOKE_REPLY: 0x09
                .writeLong(0) // 时间戳
                .writeShort(1) // 消息id
                .writeShort(deviceId.getBytes().length) // 设备id长度
                .writeBytes(deviceId.getBytes())        // 设备id
                .writeShort(functionId.getBytes().length)
                .writeBytes(functionId.getBytes())
                .writeByte(0x00) // 失败标识 0x00=失败, 0x01=成功
                .writeByte(0x0b) // code STRING type
                .writeShort(code.getBytes().length) // 错误码长度
                .writeBytes(code.getBytes())        // 错误码 "101"
                .writeByte(0x0b)  // message STRING type
                .writeShort(message.getBytes().length) // 错误消息长度
                .writeBytes(message.getBytes())     // 错误消息 "device offline"
                .writeBytes("\r\n".getBytes());     // 拆粘包分隔符

        ByteBuf buf = Unpooled.buffer()
                .writeInt(0) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));

        buf.readInt();
        //System.out.println("buf after length: " + buf.readableBytes());
        DeviceMessage read = BinaryMessageType.read(buf);
        System.out.println(read);
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
        //将长度字节读取截断后，直接解析报文正文
        //System.out.println("buf before length: " + buf.readableBytes());
        buf.readInt();
        //System.out.println("buf after length: " + buf.readableBytes());
        DeviceMessage read = BinaryMessageType.read(buf);
        if (null != read.getHeaders()) {
            read.getHeaders().forEach(message::addHeader);
        }

        System.out.println(read);
        Assert.assertEquals(read.toString(), message.toString());
    }

    /**
     * decode: hex dump string convert to DeviceMessage
     */
    @Test
    public void testMessageDecode() {
        String hexString = "0000003c08000001987e0e3e53000100083131313130303031000d4c4f434b5f4f50454e5f434d4400020004706f72740b0001340004747970650b0001310d0a";
        //String jsonString = "{\"headers\":{\"_seq\":1},\"messageType\":\"REPORT_PROPERTY\",\"messageId\":\"1\",\"deviceId\":\"1946042226889179136\",\"properties\":{\"CHARGE_STATE\":{\"num\":6,\"state\":\"001100\"},\"LOCK_STATE\":{\"num\":6,\"state\":\"001110\"}},\"timestamp\":1753669721245}";

        //String hexString = "0000002102000001984EEA5F3A000100133139343630343232323638383931373931333600";
        byte[] bytes = ByteBufUtil.decodeHexDump(hexString);

        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        // 截断消息长度 4bytes
        buf.readInt();

        DeviceMessage message = BinaryMessageType.read(buf);
        System.out.println(message); //{"headers":{"_seq":1,"code":"ok"},"messageType":"ACKNOWLEDGE","success":true,"messageId":"1","deviceId":"1946042226889179136","timestamp":1753670639418}
        //System.out.println(jsonString);
    }

    @Test
    public void testMessageEncode() {
        String hexString = "0000007603000001984edc5c9d00010013313934363034323232363838393137393133360002000c4348415247455f53544154450e000200036e756d0400000006000573746174650b0006303031313030000a4c4f434b5f53544154450e000200036e756d0400000006000573746174650b0006303031313130";
        String jsonString = "{\"headers\":{\"_seq\":1},\"messageType\":\"REPORT_PROPERTY\",\"messageId\":\"1\",\"deviceId\":\"1946042226889179136\",\"properties\":{\"CHARGE_STATE\":{\"num\":6,\"state\":\"001100\"},\"LOCK_STATE\":{\"num\":6,\"state\":\"001110\"}},\"timestamp\":1753669721245}";

        DeviceMessage message = JSON.parseObject(jsonString, ReportPropertyMessage.class);

        ByteBuf data = BinaryMessageType.write(message, Unpooled.buffer());

        ByteBuf buf = Unpooled.buffer().writeInt(data.readableBytes()).writeBytes(data);

        System.out.println(ByteBufUtil.hexDump(buf));
        System.out.println(hexString);
    }

    private void wrapper(ByteBuf data) {
        ByteBuf buf = Unpooled.buffer()
                .writeInt(data.readableBytes()) // 消息长度
                .writeBytes(data);              // 消息内容

        System.out.println(ByteBufUtil.prettyHexDump(buf));
        System.out.println(ByteBufUtil.hexDump(buf));
    }

    private void read(ByteBuf data) {
        //将长度字节读取后，直接解析报文正文
        data.readInt();
        DeviceMessage read = BinaryMessageType.read(data);
        System.out.println(read);
    }
}