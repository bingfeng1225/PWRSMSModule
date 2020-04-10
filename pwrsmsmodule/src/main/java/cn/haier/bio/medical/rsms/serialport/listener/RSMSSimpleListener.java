package cn.haier.bio.medical.rsms.serialport.listener;


import java.io.IOException;

import cn.haier.bio.medical.rsms.entity.recv.RSMSResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;

public class RSMSSimpleListener implements IRSMSListener {
    @Override
    public void onRSMSConnected() {

    }

    @Override
    public void onRSMSException() {

    }

    @Override
    public void onRSMSStatusReceived(RSMSQueryStatusResponseEntity status) throws IOException {

    }

    @Override
    public void onRSMSNetworkReceived(RSMSNetworkResponseEntity network) throws IOException {

    }

    @Override
    public void onRSMSModulesReceived(RSMSQueryModulesResponseEntity modules) throws IOException {

    }

    @Override
    public void onRSMSUnknownReceived() throws IOException {

    }

    @Override
    public void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) throws IOException {

    }

    @Override
    public void onRSMSTransmissionReceived(RSMSTransmissionEntity entity) throws IOException {

    }

    @Override
    public void onRSMSRecoveryReceived(RSMSResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSClearCacheReceived(RSMSResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSDTEModelConfigReceived(RSMSResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSQuitConfigReceived(RSMSResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) throws IOException {

    }
}
