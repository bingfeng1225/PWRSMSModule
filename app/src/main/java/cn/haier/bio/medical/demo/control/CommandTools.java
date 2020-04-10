package cn.haier.bio.medical.demo.control;

import java.nio.ByteOrder;

import cn.haier.bio.medical.demo.control.recv.TemptureCommandEntity;
import cn.qd.peiwen.pwtools.ByteUtils;

public class CommandTools {
    public static final int DEVICE_TYPE = 0x01;
    public static final int PROTOCOL_VERSION = 0x00;

    public static TemptureCommandEntity parseTemptureCommandEntity(byte[] data){
        TemptureCommandEntity entity = new TemptureCommandEntity();
        entity.setTempture(ByteUtils.bytes2Short(data, ByteOrder.LITTLE_ENDIAN));
        return entity;
    }
}
