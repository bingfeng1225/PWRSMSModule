package cn.haier.bio.medical.rsms.entity.send.client;

import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.tools.RSMSTools;

public abstract class RSMSCollectionEntity extends RSMSSendBaseEntity {
    protected byte dataType; //统计数据类型：设备数据、用户数据、控制指令应答数据
    protected int deviceType;//设备类型
    protected int protocolVersion;//协议版本

    public RSMSCollectionEntity() {
        super(RSMSTools.RSMS_COMMAND_COLLECTION_DATA);
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * 数据采集协议框架各类数据数据段封装函数
     *
     * @return byte[] 设备数据、用户数据、控制指令应答数据中数据段的字节数组
     */
    public abstract byte[] packageCollectionMessage();
}
