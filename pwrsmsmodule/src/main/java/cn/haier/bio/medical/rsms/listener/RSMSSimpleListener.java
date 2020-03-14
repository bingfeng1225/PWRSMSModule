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

public class RSMSSimpleListener implements IRSMSListener {
    @Override
    public void onRSMSConnected() {

    }

    @Override
    public void onRSMSException() {

    }

    @Override
    public void onMessageSended(String data) {

    }

    @Override
    public void onMessageRecved(String data) {

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
    public void onRSMSPDAModulesReceived(RSMSQueryPDAModulesResponseEntity modules) throws IOException {

    }

    @Override
    public void onRSMSUnknownReceived() throws IOException {

    }

    @Override
    public void onRSMSControlReceived(RSMSControlCommandEntity entity) throws IOException {

    }

    @Override
    public void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) throws IOException {

    }

    @Override
    public void onRSMSRecoveryReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSClearCacheReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSQuitConfigReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSAModelConfigReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSBModelConfigReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSDTEModelConfigReceived(RSMSCommontResponseEntity response) throws IOException {

    }

    @Override
    public void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) throws IOException {

    }
}
