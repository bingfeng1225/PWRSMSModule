package cn.haier.bio.medical.rsms.entity.recv;

import cn.haier.bio.medical.rsms.tools.RSMSTools;

public class RSMSEnterConfigResponseEntity extends RSMSRecvBaseEntity {
    private byte response;
    private byte configModel;

    public RSMSEnterConfigResponseEntity() {
        super(RSMSTools.RSMS_RESPONSE_ENTER_CONFIG);
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
