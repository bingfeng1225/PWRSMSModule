package cn.haier.bio.medical.rsms;

import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;

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
    void onControlCommandReceived(RSMSCommandEntity command);
    void onNetworkReceived(RSMSNetworkResponseEntity network);
    void onStatusReceived(RSMSQueryStatusResponseEntity status);
    void onModulesReceived(RSMSQueryModulesResponseEntity modules);
    boolean checkControlCommand(int deviceType,int protocolVersion,int controlCommand);
}
