package cn.haier.bio.medical.rsms.entity.recv;

public class RSMSResponseEntity extends RSMSRecvBaseEntity {
    private byte response;

    public RSMSResponseEntity() {

    }

    public byte getResponse() {
        return response;
    }

    public void setResponse(byte response) {
        this.response = response;
    }
}
