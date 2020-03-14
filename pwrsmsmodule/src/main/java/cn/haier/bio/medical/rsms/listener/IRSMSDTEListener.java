package cn.haier.bio.medical.rsms.listener;

import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;

public interface IRSMSDTEListener {
    String findDeviceCode();
    void onPDAConfigQuited();
    void onDTEConfigQuited();
    void onDTEConfigEntered();
    void onPDAConfigEntered();
    void onRecoverySuccessed();
    void onClearCacheSuccessed();
    void onDETMacChanged(String mac);
    void onDeviceCodeChanged(String code);
    void onNetworkReceived(RSMSNetworkResponseEntity network);
    void onStatusReceived(RSMSQueryStatusResponseEntity status);
    void onModulesReceived(RSMSQueryModulesResponseEntity modules);
}
