package cn.haier.bio.medical.rsms;

import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;

public interface IRSMSDTEListener {
    void onDTEReady();
    String findDeviceCode();
    void onPDAConfigQuited();
    void onDTEConfigQuited();
    void onDTEConfigEntered();
    void onPDAConfigEntered();
    void onRecoverySuccessed();
    void onClearCacheSuccessed();
    void onDTEPrint(String message);
    void onDETMacChanged(String mac);
    void onDeviceCodeChanged(String code);
    void onDateTimeChanged(long timestamp);
    void onDTEException(Throwable throwable);
    void onControlCommandReceived(RSMSCommandEntity command);
    void onNetworkReceived(RSMSNetworkResponseEntity network);
    void onStatusReceived(RSMSQueryStatusResponseEntity status);
    void onModulesReceived(RSMSQueryModulesResponseEntity modules);
    boolean checkControlCommand(int deviceType,int protocolVersion,int controlCommand);
}
