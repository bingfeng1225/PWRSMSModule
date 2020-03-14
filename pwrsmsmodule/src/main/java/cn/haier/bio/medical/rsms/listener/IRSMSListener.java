package cn.haier.bio.medical.rsms.listener;


import java.io.IOException;

import cn.haier.bio.medical.rsms.entity.recv.RSMSCommontResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSControlCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryPDAModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;

public interface IRSMSListener {
    void onRSMSConnected();
    void onRSMSException();
    void onMessageSended(String data);
    void onMessageRecved(String data);
    void onRSMSStatusReceived(RSMSQueryStatusResponseEntity status) throws IOException;
    void onRSMSNetworkReceived(RSMSNetworkResponseEntity network) throws IOException;
    void onRSMSModulesReceived(RSMSQueryModulesResponseEntity modules) throws IOException;
    void onRSMSPDAModulesReceived(RSMSQueryPDAModulesResponseEntity modules) throws IOException;

    void onRSMSUnknownReceived() throws IOException;
    void onRSMSControlReceived(RSMSControlCommandEntity entity) throws IOException;
    void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) throws IOException;
    void onRSMSRecoveryReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSClearCacheReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSQuitConfigReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSAModelConfigReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSBModelConfigReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSDTEModelConfigReceived(RSMSCommontResponseEntity response) throws IOException;
    void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) throws IOException;
}
