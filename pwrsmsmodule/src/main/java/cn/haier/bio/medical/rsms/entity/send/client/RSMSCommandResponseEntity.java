package cn.haier.bio.medical.rsms.entity.send.client;

import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwtools.EmptyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSCommandResponseEntity extends RSMSCollectionEntity {
    protected int command; //指令类型
    protected byte handleState;//处理状态
    protected long identification;//指令标识

    public RSMSCommandResponseEntity(RSMSCommandEntity entity) {
        this.command = entity.getCommand();
        this.deviceType = entity.getDeviceType();
        this.identification = entity.getIdentification();
        this.protocolVersion = entity.getProtocolVersion();
        this.dataType = RSMSTools.COLLECTION_CONTROL_RESPONSE_TYPE;
    }

    public byte getHandleState() {
        return handleState;
    }

    public void setHandleState(byte handleState) {
        this.handleState = handleState;
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

        buffer.writeShortLE(this.command);
        buffer.writeLongLE(this.identification);
        buffer.writeByte(this.handleState);

        //指令处理结果中数据段封装
        byte[] message = this.packageCollectionMessage();
        if (EmptyUtils.isNotEmpty(message)) {
            buffer.writeBytes(message, 0, message.length);
        }

        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, buffer.readableBytes());
        buffer.release();
        return data;
    }


}
