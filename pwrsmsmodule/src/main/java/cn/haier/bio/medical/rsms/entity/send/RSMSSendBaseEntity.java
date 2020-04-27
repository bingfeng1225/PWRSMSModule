package cn.haier.bio.medical.rsms.entity.send;

public class RSMSSendBaseEntity {
    protected final short commandType;
    protected final boolean needResponse;

    public RSMSSendBaseEntity(short commandType) {
        this(commandType,true);
    }

    public RSMSSendBaseEntity(short commandType, boolean needResponse) {
        this.commandType = commandType;
        this.needResponse = needResponse;
    }

    public short getCommandType() {
        return commandType;
    }

    public boolean isNeedResponse() {
        return needResponse;
    }

    public byte[] packageSendMessage() {
        return new byte[0];
    }
}
