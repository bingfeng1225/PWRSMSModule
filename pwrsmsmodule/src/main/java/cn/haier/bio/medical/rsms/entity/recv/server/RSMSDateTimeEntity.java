package cn.haier.bio.medical.rsms.entity.recv.server;

public class RSMSDateTimeEntity extends RSMSTransmissionEntity {
    private long timestamp;

    public RSMSDateTimeEntity() {

    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
