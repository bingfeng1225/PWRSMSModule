package cn.haier.bio.medical.rsms.socket;

public interface IRSMSSettingListener {
    void onConnected();
    void onDisconnected();
    void onConfigSuccessed();
    void onAModelConfigEntered();
    void onBModelConfigEntered();
}
