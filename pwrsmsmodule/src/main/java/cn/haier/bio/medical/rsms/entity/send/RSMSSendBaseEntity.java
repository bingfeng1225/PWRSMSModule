package cn.haier.bio.medical.rsms.entity.send;

public class RSMSSendBaseEntity {
    protected final int commandType;
    protected final boolean needResponse;

    public RSMSSendBaseEntity(int commandType) {
        this(commandType,true);
    }

    public RSMSSendBaseEntity(int commandType, boolean needResponse) {
        this.commandType = commandType;
        this.needResponse = needResponse;
    }

    public int getCommandType() {
        return commandType;
    }

    public boolean isNeedResponse() {
        return needResponse;
    }

    public byte[] packageSendMessage() {
        return new byte[0];
    }
}
