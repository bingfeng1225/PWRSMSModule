package cn.haier.bio.medical.rsms.tools;

import java.net.NetworkInterface;
import java.util.Enumeration;

import cn.haier.bio.medical.rsms.entity.recv.RSMSResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSDateTimeEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSTools {
    public static final byte DEVICE = (byte) 0xA0;
    public static final byte DTE_CONFIG = (byte) 0xB0;
    public static final byte PDA_CONFIG = (byte) 0xB1;

    public static final byte COLLECTION_DATE_TYPE = (byte) 0x84;
    public static final byte COLLECTION_DATA_TYPE = (byte) 0x85;
    public static final byte COLLECTION_EVENT_TYPE = (byte) 0x86;
    public static final byte COLLECTION_CONTROL_COMMAND_TYPE = (byte) 0x87;
    public static final byte COLLECTION_CONTROL_RESPONSE_TYPE = (byte) 0x88;

    public static final byte[] HEADER = {(byte) 0x55, (byte) 0xAA};
    public static final byte[] TAILER = {(byte) 0xEA, (byte) 0xEE};
    public static final byte[] DEFAULT_MAC = {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };
    public static final byte[] DEFAULT_BE_CODE = {
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20
    };

    public static final int RSMS_COMMAND_QUERY_STATUS = 0x1101;
    public static final int RSMS_RESPONSE_QUERY_STATUS = 0x1201;

    public static final int RSMS_COMMAND_QUERY_NETWORK = 0x1102;
    public static final int RSMS_RESPONSE_QUERY_NETWORK = 0x1202;

    public static final int RSMS_COMMAND_QUERY_MODULES = 0x1103;
    public static final int RSMS_RESPONSE_QUERY_MODULES = 0x1203;

    public static final int RSMS_COMMAND_ENTER_CONFIG = 0x1301;
    public static final int RSMS_RESPONSE_ENTER_CONFIG = 0x1401;

    public static final int RSMS_COMMAND_CONFIG_QUIT = 0x1302;
    public static final int RSMS_RESPONSE_CONFIG_QUIT = 0x1402;

    public static final int RSMS_COMMAND_CONFIG_DTE_MODEL = 0x1303;
    public static final int RSMS_RESPONSE_CONFIG_DTE_MODEL = 0x1403;

    public static final int RSMS_COMMAND_CONFIG_RECOVERY = 0x1306;
    public static final int RSMS_RESPONSE_CONFIG_RECOVERY = 0x1406;

    public static final int RSMS_COMMAND_CONFIG_CLEAR_CACHE = 0x1307;
    public static final int RSMS_RESPONSE_CONFIG_CLEAR_CACHE = 0x1407;

    public static final int RSMS_COMMAND_COLLECTION_DATA = 0x1501;
    public static final int RSMS_RESPONSE_COLLECTION_DATA = 0x1601;

    public static final int RSMS_TRANSMISSION_COMMAND = (short)0xC001;
    public static final int RSMS_TRANSMISSION_RESPONSE = (short)0xC101;

    public static boolean checkFrame(byte[] data) {
        byte check = data[data.length - 3];
        byte l8sum = computeL8SumCode(data, 2, data.length - 5);
        return (check == l8sum);
    }

    public static byte[] generateMacAddress() {
        byte[] mac = getMachineHardwareAddress();
        if (mac == null || mac.length != 6) {
            return DEFAULT_MAC;
        }
        return mac;
    }

    public static String generateCode(String code) {
        if (code == null || !code.startsWith("BE") || code.getBytes().length != 20) {
            return new String(DEFAULT_BE_CODE);
        }
        return code;
    }

    public static byte[] packageString(String src) {
        ByteBuf buffer = Unpooled.buffer(22);
        buffer.writeByte('\"');
        if (null != src && src.length() > 0) {
            byte[] bytes = src.getBytes();
            buffer.writeBytes(bytes, 0, bytes.length);
        }
        buffer.writeByte('\"');
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, buffer.readableBytes());
        buffer.release();
        return data;
    }

    public static int indexOf(ByteBuf haystack, byte needle) {
        //遍历haystack的每一个字节
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
            if(needle == haystack.getByte(i)){
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(ByteBuf haystack, byte[] needle) {
        //遍历haystack的每一个字节
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
            int needleIndex;
            int haystackIndex = i;
            /*haystack是否出现了delimiter，注意delimiter是一个ChannelBuffer（byte[]）
            例如对于haystack="ABC\r\nDEF"，needle="\r\n"
            那么当haystackIndex=3时，找到了“\r”，此时needleIndex=0
            继续执行循环，haystackIndex++，needleIndex++，
            找到了“\n”
            至此，整个needle都匹配到了
            程序然后执行到if (needleIndex == needle.capacity())，返回结果
            */
            for (needleIndex = 0; needleIndex < needle.length; needleIndex++) {
                if (haystack.getByte(haystackIndex) != needle[needleIndex]) {
                    break;
                } else {
                    haystackIndex++;
                    if (haystackIndex == haystack.writerIndex() && needleIndex != needle.length - 1) {
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.length) {
                // Found the needle from the haystack!
                return i - haystack.readerIndex();
            }
        }
        return -1;
    }

    public static byte[] packageCommand(RSMSSendBaseEntity entity) {
        ByteBuf buffer = Unpooled.buffer(8);
        buffer.writeBytes(HEADER, 0, HEADER.length); //帧头 2位
        byte[] buf = (entity == null) ? new byte[0] : entity.packageSendMessage();
        //数据长度 = type(1) + cmd(1) + device(1) + entity(n) + check(1)
        buffer.writeShort(4 + buf.length); //长度 2位
        buffer.writeShort(entity.getCommandType());   //2位
        buffer.writeByte(DEVICE);  //1位

        buffer.writeBytes(buf, 0, buf.length); //其他参数 N位

        byte l8sum = computeL8SumCode(buffer.array(), 2, buffer.readableBytes() - 2);
        buffer.writeByte(l8sum);  //校验和  1位
        buffer.writeBytes(TAILER, 0, TAILER.length); //帧尾 2位

        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, data.length);
        buffer.release();
        return data;
    }

    public static RSMSQueryStatusResponseEntity parseRSMSStatusEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);

        RSMSQueryStatusResponseEntity entity = new RSMSQueryStatusResponseEntity();

        entity.setModel(buffer.readByte());

        entity.setStatus(buffer.readByte());
        entity.setEncode(buffer.readByte());

        entity.setLevel(buffer.readByte());
        entity.setWifiLevel(buffer.readByte());

        entity.setUploadFrequency(buffer.readShort());
        entity.setAcquisitionFrequency(buffer.readShort());

        entity.setYear(buffer.readByte());
        entity.setMonth(buffer.readByte());
        entity.setDay(buffer.readByte());
        entity.setHour(buffer.readByte());
        entity.setMinute(buffer.readByte());
        entity.setSecond(buffer.readByte());

        return entity;
    }

    public static RSMSNetworkResponseEntity parseRSMSNetworkEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);

        RSMSNetworkResponseEntity entity = new RSMSNetworkResponseEntity();

        entity.setModel(buffer.readByte());

        entity.setAddress(parseString(buffer));
        entity.setPort(parseString(buffer));

        entity.setWifiName(parseString(buffer));
        entity.setWifiPassword(parseString(buffer));

        entity.setApn(parseString(buffer));
        entity.setApnName(parseString(buffer));
        entity.setApnPassword(parseString(buffer));

        return entity;
    }

    public static RSMSQueryModulesResponseEntity parseRSMSModulesEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);

        RSMSQueryModulesResponseEntity entity = new RSMSQueryModulesResponseEntity();
        byte[] mcu = new byte[12];
        buffer.readBytes(mcu, 0, mcu.length);
        entity.setMcu(mcu);

        byte[] mac = new byte[6];
        buffer.readBytes(mac, 0, mac.length);
        entity.setMac(mac);

        entity.setCode(parseString(buffer));

        entity.setImei(parseString(buffer));
        entity.setIccid(parseString(buffer));
        entity.setPhone(parseString(buffer));

        entity.setModuleVersion(parseString(buffer));
        entity.setWifiVersion(parseString(buffer));
        entity.setMcuVersion(parseString(buffer));

        entity.setOperator(parseString(buffer));
        return entity;
    }

    public static RSMSResponseEntity parseRSMSResponseEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);
        RSMSResponseEntity entity = new RSMSResponseEntity();
        entity.setResponse(buffer.readByte());
        return entity;
    }

    public static RSMSEnterConfigResponseEntity parseRSMSConfigModelResponseEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);

        RSMSEnterConfigResponseEntity entity = new RSMSEnterConfigResponseEntity();
        entity.setConfigModel(buffer.readByte());
        entity.setResponse(buffer.readByte());

        return entity;
    }

    public static RSMSTransmissionEntity parseRSMSTransmissionEntity(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(6);
        byte year = buffer.readByte();
        byte month = buffer.readByte();
        byte day = buffer.readByte();
        byte hour = buffer.readByte();
        byte minute = buffer.readByte();
        byte second = buffer.readByte();
        byte dataType = buffer.readByte();
        if(dataType == COLLECTION_DATE_TYPE){
            RSMSDateTimeEntity entity = new RSMSDateTimeEntity();
            entity.setYear(year);
            entity.setMonth(month);
            entity.setDay(day);
            entity.setHour(hour);
            entity.setMinute(minute);
            entity.setSecond(second);

            entity.setDataType(dataType);
            entity.setDeviceType(buffer.readShortLE());
            entity.setProtocolVersion(buffer.readShortLE());

            entity.setTimestamp(buffer.readLongLE());
            return entity;
        }else if(dataType == COLLECTION_CONTROL_COMMAND_TYPE){
            RSMSCommandEntity entity = new RSMSCommandEntity();
            entity.setYear(year);
            entity.setMonth(month);
            entity.setDay(day);
            entity.setHour(hour);
            entity.setMinute(minute);
            entity.setSecond(second);

            entity.setDataType(dataType);

            entity.setDeviceType(buffer.readShortLE());
            entity.setProtocolVersion(buffer.readShortLE());
            entity.setCommand(buffer.readShortLE());
            entity.setIdentification(buffer.readLongLE());

            int read = buffer.readerIndex();
            int write = buffer.writerIndex();
            byte[] control = new byte[write - read - 3];
            buffer.readBytes(control, 0, control.length);
            entity.setControl(control);
            return entity;
        }else{
            RSMSTransmissionEntity entity = new RSMSTransmissionEntity();
            entity.setYear(year);
            entity.setMonth(month);
            entity.setDay(day);
            entity.setHour(hour);
            entity.setMinute(minute);
            entity.setSecond(second);
            entity.setDataType(dataType);
            return entity;
        }
    }

    public static String command2String(short command){
        byte[] bytes = short2Bytes(command);
        return bytes2HexString(bytes,true,",");
    }

    public static byte[] short2Bytes(short value) {
        byte bytes[] = new byte[2];
        bytes[0] = (byte) (0xff & (value >> 8));
        bytes[1] = (byte) (0xff & value);
        return bytes;
    }

    public static String bytes2HexString(byte[] data) {
        return bytes2HexString(data, false);
    }

    public static String bytes2HexString(byte[] data, boolean hexFlag) {
        return bytes2HexString(data, hexFlag, null);
    }

    public static String bytes2HexString(byte[] data, boolean hexFlag, String separator) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        return bytes2HexString(data, 0, data.length, hexFlag, separator);
    }

    public static String bytes2HexString(byte[] data, int offset, int len) {
        return bytes2HexString(data, offset, len, false);
    }

    public static String bytes2HexString(byte[] data, int offset, int len, boolean hexFlag) {
        return bytes2HexString(data, offset, len, hexFlag, null);
    }

    public static String bytes2HexString(byte[] data, int offset, int len, boolean hexFlag, String separator) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        if (offset < 0 || offset > data.length - 1) {
            throw new IllegalArgumentException("The offset index out of bounds");
        }
        if (len < 0 || offset + len > data.length) {
            throw new IllegalArgumentException("The len can not be < 0 or (offset + len) index out of bounds");
        }
        String format = "%02X";
        if (hexFlag) {
            format = "0x%02X";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = offset; i < offset + len; i++) {
            buffer.append(String.format(format, data[i]));
            if (separator == null) {
                continue;
            }
            if (i != (offset + len - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }

    //校验和取低8位算法
    public static byte computeL8SumCode(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        return computeL8SumCode(data, 0, data.length);
    }

    public static byte computeL8SumCode(byte[] data, int offset, int len) {
        if (data == null) {
            throw new IllegalArgumentException("The data can not be blank");
        }
        if (offset < 0 || offset > data.length - 1) {
            throw new IllegalArgumentException("The offset index out of bounds");
        }
        if (len < 0 || offset + len > data.length) {
            throw new IllegalArgumentException("The len can not be < 0 or (offset + len) index out of bounds");
        }
        int sum = 0;
        for (int pos = offset; pos < offset + len; pos++) {
            sum += data[pos];
        }
        return (byte) sum;
    }





    private static String parseString(ByteBuf buffer) {
        buffer.skipBytes(1); //跳过头\"
        int index = indexOf(buffer, (byte) ('\"'));
        byte[] data = new byte[index - buffer.readerIndex()];
        buffer.readBytes(data, 0, data.length);
        buffer.skipBytes(1);//跳过尾\"
        return new String(data);
    }

    private static byte[] getMachineHardwareAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface element = interfaces.nextElement();
                if ("wlan0".equals(element.getName())) {
                    return element.getHardwareAddress();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
