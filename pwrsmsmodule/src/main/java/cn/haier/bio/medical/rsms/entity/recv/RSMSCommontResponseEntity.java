package cn.haier.bio.medical.rsms.entity.recv;

public class RSMSCommontResponseEntity extends RSMSRecvBaseEntity {
    private byte response;

    public RSMSCommontResponseEntity() {

    }

    public byte getResponse() {
        return response;
    }

    public void setResponse(byte response) {
        this.response = response;
    }
}
