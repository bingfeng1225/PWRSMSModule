package cn.haier.bio.medical.rsms.serialport.listener;


import java.io.IOException;

import cn.haier.bio.medical.rsms.entity.recv.RSMSResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;

public interface IRSMSListener {
    void onRSMSConnected();
    void onRSMSPrint(String message);
    void onRSMSException(Throwable throwable);
    void onRSMSStatusReceived(RSMSQueryStatusResponseEntity status) throws IOException;
    void onRSMSNetworkReceived(RSMSNetworkResponseEntity network) throws IOException;
    void onRSMSModulesReceived(RSMSQueryModulesResponseEntity modules) throws IOException;

    void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) throws IOException;
    void onRSMSTransmissionReceived(RSMSTransmissionEntity entity) throws IOException;
    void onRSMSRecoveryReceived(RSMSResponseEntity response) throws IOException;
    void onRSMSClearCacheReceived(RSMSResponseEntity response) throws IOException;
    void onRSMSQuitConfigReceived(RSMSResponseEntity response) throws IOException;
    void onRSMSDTEModelConfigReceived(RSMSResponseEntity response) throws IOException;
    void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) throws IOException;
}
