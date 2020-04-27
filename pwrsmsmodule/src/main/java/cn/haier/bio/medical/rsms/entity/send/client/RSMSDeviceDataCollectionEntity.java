package cn.haier.bio.medical.rsms.entity.send.client;

import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwtools.EmptyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSDeviceDataCollectionEntity extends RSMSCollectionEntity {
    public RSMSDeviceDataCollectionEntity() {
        this.dataType = RSMSTools.COLLECTION_DATA_TYPE;
    }

    @Override
    public byte[] packageCollectionMessage() {
        return new byte[0];
    }

    @Override
    public byte[] packageSendMessage() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(this.dataType);
        buffer.writeShortLE(this.deviceType);
        buffer.writeShortLE(this.protocolVersion);
        //设备数据协议中数据段的封装
        byte[] message = this.packageCollectionMessage();
        if(EmptyUtils.isNotEmpty(message)){
            buffer.writeBytes(message,0,message.length);
        }

        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, buffer.readableBytes());
        buffer.release();
        return data;
    }
}
