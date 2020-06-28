package cn.haier.bio.medical.rsms.entity.send.client;

import cn.haier.bio.medical.rsms.tools.RSMSTools;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSDateTimeCollectionEntity extends RSMSCollectionEntity {
    public RSMSDateTimeCollectionEntity() {
        this.dataType = RSMSTools.COLLECTION_DATE_TYPE;
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
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, buffer.readableBytes());
        buffer.release();
        return data;
    }
}
