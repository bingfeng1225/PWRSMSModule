package cn.haier.bio.medical.rsms.entity.recv.server;

import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;

public class RSMSTransmissionEntity extends RSMSRecvBaseEntity {
    private byte year; //年
    private byte month;//月
    private byte day;//日
    private byte hour;//时
    private byte minute;//分
    private byte second;//秒
    private byte dataType; //数据类型
    private int deviceType;//设备类型
    private int protocolVersion;//协议版本


    public RSMSTransmissionEntity() {

    }

    public byte getYear() {
        return year;
    }

    public void setYear(byte year) {
        this.year = year;
    }

    public byte getMonth() {
        return month;
    }

    public void setMonth(byte month) {
        this.month = month;
    }

    public byte getDay() {
        return day;
    }

    public void setDay(byte day) {
        this.day = day;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        this.hour = hour;
    }

    public byte getMinute() {
        return minute;
    }

    public void setMinute(byte minute) {
        this.minute = minute;
    }

    public byte getSecond() {
        return second;
    }

    public void setSecond(byte second) {
        this.second = second;
    }

    public byte getDataType() {
        return dataType;
    }

    public void setDataType(byte dataType) {
        this.dataType = dataType;
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
}
