package cn.haier.bio.medical.rsms.entity.recv;

public class RSMSEnterConfigResponseEntity extends RSMSRecvBaseEntity {
    private byte response;
    private byte configModel;

    public RSMSEnterConfigResponseEntity() {

    }

    public byte getResponse() {
        return response;
    }

    public void setResponse(byte response) {
        this.response = response;
    }

    public byte getConfigModel() {
        return configModel;
    }

    public void setConfigModel(byte configModel) {
        this.configModel = configModel;
    }
}
