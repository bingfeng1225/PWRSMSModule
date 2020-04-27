package cn.haier.bio.medical.rsms.entity.recv;

import cn.haier.bio.medical.rsms.tools.RSMSTools;

public class RSMSQueryStatusResponseEntity extends RSMSRecvBaseEntity {
    private byte model; //联网模式
    private byte status;//模块状态码
    private byte encode;//编码规则状态码
    private byte level; //4G信号强度
    private byte wifiLevel; //WIFI信号强度
    private int uploadFrequency;//上传频率
    private int acquisitionFrequency;//采集频率
    private byte year; //年
    private byte month;//月
    private byte day;//日
    private byte hour;//时
    private byte minute;//分
    private byte second;//秒

    public RSMSQueryStatusResponseEntity() {
        super(RSMSTools.RSMS_RESPONSE_QUERY_STATUS);
    }

    public byte getModel() {
        return model;
    }

    public void setModel(byte model) {
        this.model = model;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getEncode() {
        return encode;
    }

    public void setEncode(byte encode) {
        this.encode = encode;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public byte getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(byte wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    public int getUploadFrequency() {
        return uploadFrequency;
    }

    public void setUploadFrequency(int uploadFrequency) {
        this.uploadFrequency = uploadFrequency;
    }

    public int getAcquisitionFrequency() {
        return acquisitionFrequency;
    }

    public void setAcquisitionFrequency(int acquisitionFrequency) {
        this.acquisitionFrequency = acquisitionFrequency;
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
}
