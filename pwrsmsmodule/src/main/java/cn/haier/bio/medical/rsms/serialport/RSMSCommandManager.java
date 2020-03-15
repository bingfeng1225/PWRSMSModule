package cn.haier.bio.medical.rsms.serialport;

import cn.haier.bio.medical.rsms.entity.send.RSMSEnterConfigModelEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSQueryStatusEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSAModelConfigEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSBModelConfigEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSDTEModelConfigEntity;
import cn.haier.bio.medical.rsms.listener.IRSMSListener;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwtools.EmptyUtils;

public class RSMSCommandManager {
    private byte[] mac;
    private RSMSSerialPort serialPort;
    private static RSMSCommandManager manager;

    public static RSMSCommandManager getInstance() {
        if (manager == null) {
            synchronized (RSMSCommandManager.class) {
                if (manager == null)
                    manager = new RSMSCommandManager();
            }
        }
        return manager;
    }

    private RSMSCommandManager() {
        this.mac = RSMSTools.generateMacAddress();
    }

    public void init(String path) {
        if (EmptyUtils.isEmpty(this.serialPort)) {
            this.serialPort = new RSMSSerialPort();
            this.serialPort.init(path);
        }
    }

    public void enable() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.enable();
        }
    }

    public void disable() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.disable();
        }
    }

    public void release() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.disable();
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    public void changeListener(IRSMSListener listener) {
        if(EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.changeListener(listener);
        }
    }

    public void queryStatus(String code) {
        RSMSQueryStatusEntity entity = new RSMSQueryStatusEntity();
        entity.setMac(this.mac);
        entity.setMcu(RSMSTools.DEFAULT_MAC);
        entity.setCode(RSMSTools.generateCode(code));
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void queryNetwork() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_QUERY_NETWORK));
        }
    }

    public void queryModules() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_QUERY_MODULES));
        }
    }

    public void queryPDAModules() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_QUERY_PDA_MODULES));
        }
    }

    public void recovery() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_CONFIG_RECOVERY));
        }
    }

    public void clearCache() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_CONFIG_CLEAR_CACHE));
        }
    }

    public void quitConfigModel() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSSendBaseEntity(RSMSTools.RSMS_COMMAND_CONFIG_QUIT));
        }
    }

    public void enterDTEConfigModel() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSEnterConfigModelEntity(false));
        }
    }

    public void enterPDAConfigModel() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(new RSMSEnterConfigModelEntity(true));
        }
    }

    public void configAModel(RSMSAModelConfigEntity entity) {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void configBModel(RSMSBModelConfigEntity entity) {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void configDTEModel(RSMSDTEModelConfigEntity entity) {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void collectionDeviceData(RSMSSendBaseEntity entity) {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }
}
