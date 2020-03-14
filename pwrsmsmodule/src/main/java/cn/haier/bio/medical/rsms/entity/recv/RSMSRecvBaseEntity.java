package cn.haier.bio.medical.rsms.entity.recv;

public class RSMSRecvBaseEntity {
    protected int commandType;

    public RSMSRecvBaseEntity() {
    }

    public RSMSRecvBaseEntity(int commandType) {
        this.commandType = commandType;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }
}
