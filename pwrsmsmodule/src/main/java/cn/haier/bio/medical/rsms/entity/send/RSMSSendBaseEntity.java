package cn.haier.bio.medical.rsms.entity.send;

public class RSMSSendBaseEntity {
    private int commandType;

    public RSMSSendBaseEntity() {

    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public byte[] packageSendMessage() {
        return new byte[0];
    }
}