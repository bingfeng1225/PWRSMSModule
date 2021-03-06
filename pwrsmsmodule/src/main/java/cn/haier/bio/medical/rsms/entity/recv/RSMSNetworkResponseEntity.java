package cn.haier.bio.medical.rsms.entity.recv;

import cn.haier.bio.medical.rsms.tools.RSMSTools;

public class RSMSNetworkResponseEntity extends RSMSRecvBaseEntity {
    private byte model;//联网模式

    private String port;//端口
    private String address;//服务器域名/IP地址

    private String wifiName;//WIFI名称
    private String wifiPassword;//WIFI密码

    private String apn;//APN
    private String apnName;//APN用户名
    private String apnPassword;//APN密码


    public RSMSNetworkResponseEntity() {
        super(RSMSTools.RSMS_RESPONSE_QUERY_NETWORK);
    }

    public byte getModel() {
        return model;
    }

    public void setModel(byte model) {
        this.model = model;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getApnName() {
        return apnName;
    }

    public void setApnName(String apnName) {
        this.apnName = apnName;
    }

    public String getApnPassword() {
        return apnPassword;
    }

    public void setApnPassword(String apnPassword) {
        this.apnPassword = apnPassword;
    }
}
