package cn.haier.bio.medical.rsms.entity.recv.server;

public class RSMSCommandEntity extends RSMSTransmissionEntity {
    private int command; //指令类型
    private byte[] control; //指令内容
    private long identification;//标识别

    public RSMSCommandEntity() {
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public byte[] getControl() {
        return control;
    }

    public void setControl(byte[] control) {
        this.control = control;
    }

    public long getIdentification() {
        return identification;
    }

    public void setIdentification(long identification) {
        this.identification = identification;
    }
}
