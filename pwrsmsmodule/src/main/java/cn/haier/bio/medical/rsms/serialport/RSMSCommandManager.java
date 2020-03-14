package cn.haier.bio.medical.rsms.serialport;

import android.os.Build;

import cn.haier.bio.medical.rsms.entity.send.RSMSEnterConfigModelEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSQueryStatusEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSAModelConfigEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSBModelConfigEentity;
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

    public void init(String path, IRSMSListener listener) {
        if (EmptyUtils.isEmpty(this.serialPort)) {
            this.serialPort = new RSMSSerialPort();
            this.serialPort.init(path, listener);
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
            this.serialPort.release();
            this.serialPort = null;
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
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_QUERY_NETWORK);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void queryModules() {
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_QUERY_MODULES);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void queryPDAModules() {
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_QUERY_PDA_MODULES);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void recovery() {
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_CONFIG_RECOVERY);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void clearCache() {
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_CONFIG_CLEAR_CACHE);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
        }
    }

    public void quitConfigModel() {
        RSMSSendBaseEntity entity = new RSMSSendBaseEntity();
        entity.setCommandType(RSMSTools.RSMS_COMMAND_CONFIG_QUIT);
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.sendCommand(entity);
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

    public void configBModel(RSMSBModelConfigEentity entity) {
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
