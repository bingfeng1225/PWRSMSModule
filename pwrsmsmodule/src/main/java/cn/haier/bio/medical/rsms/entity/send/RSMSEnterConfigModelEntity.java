package cn.haier.bio.medical.rsms.entity.send;


import cn.haier.bio.medical.rsms.tools.RSMSTools;

public class RSMSEnterConfigModelEntity extends RSMSSendBaseEntity {
    private boolean pda;

    public RSMSEnterConfigModelEntity(boolean pda) {
        this.pda = pda;
    }

    @Override
    public byte[] packageSendMessage() {
        if(!this.pda) {
            return new byte[]{RSMSTools.DTE_CONFIG};
        }else{
            return new byte[]{RSMSTools.PDA_CONFIG};
        }
    }
}
