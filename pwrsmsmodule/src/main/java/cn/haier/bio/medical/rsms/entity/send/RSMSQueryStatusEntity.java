package cn.haier.bio.medical.rsms.entity.send;

import cn.haier.bio.medical.rsms.tools.RSMSTools;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSQueryStatusEntity extends RSMSSendBaseEntity {
    private String code;
    private byte[] mac;
    private byte[] mcu;
    private boolean fromUser;

    public RSMSQueryStatusEntity() {
        super((short) RSMSTools.RSMS_COMMAND_QUERY_STATUS);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public byte[] getMcu() {
        return mcu;
    }

    public void setMcu(byte[] mcu) {
        this.mcu = mcu;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    @Override
    public byte[] packageSendMessage() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(this.mac);
        buffer.writeBytes(this.mcu);
        buffer.writeBytes(RSMSTools.packageString(this.code));
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data, 0, buffer.readableBytes());
        buffer.release();
        return data;
    }
}
