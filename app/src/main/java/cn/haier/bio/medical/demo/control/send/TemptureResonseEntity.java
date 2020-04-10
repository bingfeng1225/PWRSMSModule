package cn.haier.bio.medical.demo.control.send;

import cn.haier.bio.medical.rsms.entity.send.client.RSMSCommandResponseEntity;

public class TemptureResonseEntity extends RSMSCommandResponseEntity {
    public TemptureResonseEntity() {

    }

    @Override
    public byte[] packageCollectionMessage() {
        return new byte[0];
    }
}
