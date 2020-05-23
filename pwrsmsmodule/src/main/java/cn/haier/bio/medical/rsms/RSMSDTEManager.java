package cn.haier.bio.medical.rsms;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.haier.bio.medical.rsms.entity.recv.RSMSResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSDateTimeEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSDTEModelConfigEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSEnterConfigModelEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSQueryStatusEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSCollectionEntity;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSCommandResponseEntity;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSDateTimeCollectionEntity;
import cn.haier.bio.medical.rsms.serialport.listener.RSMSSimpleListener;
import cn.haier.bio.medical.rsms.serialport.RSMSSerialPort;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwlogger.PWLogger;
import cn.qd.peiwen.pwtools.ByteUtils;
import cn.qd.peiwen.pwtools.EmptyUtils;

public class RSMSDTEManager extends RSMSSimpleListener {
    private byte[] mac;
    private String dceMac;
    private long lastTime = 0;
    private boolean pda = false;
    private boolean dte = false;
    private boolean dateTime = false;
    private RSMSQueryStatusResponseEntity status;

    private RSMSHandler handler;
    private HandlerThread thread;
    private RSMSSerialPort serialPort;
    private static RSMSDTEManager manager;
    private WeakReference<IRSMSDTEListener> listener;

    private int state;
    private boolean taskRuning = false;
    private RSMSSendBaseEntity sendBase;
    private List<RSMSSendBaseEntity> tasks;

    private static final int RSMS_STATE_IDLE = 0x01;
    private static final int RSMS_STATE_STRTUP = 0x02;
    private static final int RSMS_STATE_RUNNING = 0x03;

    private static final int RSMS_INSERT_TASK_MSG = 0x01;
    private static final int RSMS_PROCESS_TASK_MSG = 0x02;
    private static final int RSMS_FINISH_TASK_MSG = 0x03;
    private static final int RSMS_QUERY_STATE_MSG = 0x04;
    private static final int RSMS_QUERY_MODULES_MSG = 0x05;
    private static final int RSMS_QUERY_DATE_TIME_MSG = 0x06;

    public static RSMSDTEManager getInstance() {
        if (manager == null) {
            synchronized (RSMSDTEManager.class) {
                if (manager == null)
                    manager = new RSMSDTEManager();
            }
        }
        return manager;
    }

    private RSMSDTEManager() {
        this.mac = RSMSTools.generateMacAddress();
    }

    public void init(String path) {
        if (!isInitialized()) {
            this.createHandler();
            this.createSerialPort(path);
            this.state = RSMS_STATE_IDLE;
        }
    }

    public void enable() {
        if (this.isInitialized()) {
            this.serialPort.enable();
        }
    }

    public void disable() {
        if (this.isInitialized()) {
            this.serialPort.disable();
        }
    }

    public void release() {
        this.disable();
        this.destroyHandler();
        this.destroySerialPort();
    }

    public void changeListener(IRSMSDTEListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    public void recovery() {
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_COMMAND_CONFIG_RECOVERY));
    }

    public void clearCache() {
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_COMMAND_CONFIG_CLEAR_CACHE));
    }

    public void queryStatus() {
        RSMSQueryStatusEntity entity = new RSMSQueryStatusEntity();
        entity.setMac(this.mac);
        entity.setFromUser(true);
        entity.setMcu(RSMSTools.DEFAULT_MAC);
        String code = null;
        if (EmptyUtils.isNotEmpty(listener)) {
            code = listener.get().findDeviceCode();
        }
        entity.setCode(RSMSTools.generateCode(code));
        this.insertTask(entity);
    }

    public void queryNetwork() {
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_COMMAND_QUERY_NETWORK));
    }

    public void queryModules() {
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_COMMAND_QUERY_MODULES));
    }

    public void quitConfigModel() {
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_COMMAND_CONFIG_QUIT));
    }

    public void enterDTEConfigModel() {
        this.insertTask(new RSMSEnterConfigModelEntity(false));
    }

    public void configDTEModel(RSMSDTEModelConfigEntity entity) {
        this.insertTask(entity);
    }

    public void collectionDeviceData(RSMSCollectionEntity entity) {
        this.insertTask(entity);
    }

    public boolean isReady() {
        if (!this.isInitialized()) {
            return false;
        }
        if (state != RSMS_STATE_RUNNING) {
            return false;
        }
        if (EmptyUtils.isEmpty(this.status)) {
            return false;
        }
        if (((this.status.getStatus() & 0x80) != 0x80)) {
            return false;
        }
        return true;
    }

    public boolean isArrivalTime() {
        if (!this.isInitialized()) {
            return false;
        }
        if (state != RSMS_STATE_RUNNING) {
            return false;
        }
        if (EmptyUtils.isEmpty(this.status)) {
            return false;
        }
        if (this.lastTime == 0) {
            return true;
        }
        long nonoTime = System.nanoTime();
        long frequency = this.status.getUploadFrequency() * 1000 * 1000 * 1000L;
        if (nonoTime - this.lastTime < frequency) {
            return false;
        } else {
            return true;
        }
    }

    private void createHandler() {
        if (EmptyUtils.isEmpty(this.thread) && EmptyUtils.isEmpty(this.handler)) {
            this.thread = new HandlerThread("RSMSDTEManager");
            this.thread.start();
            this.handler = new RSMSHandler(this.thread.getLooper());
            this.tasks = new ArrayList<>();
        }
    }

    private void destroyHandler() {
        if (EmptyUtils.isNotEmpty(this.thread)) {
            this.thread.quitSafely();
            this.thread = null;
            this.handler = null;
            this.tasks.clear();
            this.tasks = null;
        }
    }

    private void createSerialPort(String path) {
        if (EmptyUtils.isEmpty(this.serialPort)) {
            this.serialPort = new RSMSSerialPort();
            this.serialPort.init(path);
            this.serialPort.changeListener(this);
        }
    }

    private void destroySerialPort() {
        if (EmptyUtils.isNotEmpty(this.serialPort)) {
            this.serialPort.release();
            this.serialPort = null;
        }
    }

    private boolean isInitialized() {
        if (EmptyUtils.isEmpty(this.handler)) {
            return false;
        }
        if (EmptyUtils.isEmpty(this.serialPort)) {
            return false;
        }
        return true;
    }

    private void enterPDAConfigModel() {
        this.insertTask(new RSMSEnterConfigModelEntity(true));
    }

    private void commandResponseReceived() {
        if (this.isInitialized()) {
            this.handler.sendEmptyMessage(RSMS_FINISH_TASK_MSG);
        }
    }

    private void insertTask(RSMSSendBaseEntity entity) {
        if (this.isInitialized()) {
            Message msg = Message.obtain();
            msg.what = RSMS_INSERT_TASK_MSG;
            msg.obj = entity;
            this.handler.sendMessage(msg);
        }
    }

    private void stopQuertState() {
        this.handler.removeMessages(RSMS_QUERY_STATE_MSG);
    }

    private void startQueryStatus() {
        this.stopQuertState();
        this.handler.sendEmptyMessageDelayed(RSMS_QUERY_STATE_MSG, 5000);
    }

    private void stopQueryModules() {
        this.handler.removeMessages(RSMS_QUERY_MODULES_MSG);
    }

    private void startQueryModules() {
        this.stopQueryModules();
        this.handler.sendEmptyMessageDelayed(RSMS_QUERY_MODULES_MSG, 2000);
    }

    private void stopQueryDateTime() {
        this.handler.removeMessages(RSMS_QUERY_DATE_TIME_MSG);
    }

    private void startQueryDateTime(int second) {
        this.stopQueryDateTime();
        this.handler.sendEmptyMessageDelayed(RSMS_QUERY_DATE_TIME_MSG, second * 1000);
    }

    private boolean checkPDAStartupModules(RSMSQueryModulesResponseEntity modules) {
        if (EmptyUtils.isEmpty(this.dceMac)) {
            //已进入PDA配置模式，还未获取到有效的MAC地址
            String mac = ByteUtils.bytes2HexString(modules.getMac());
            if (mac.equals("FFFFFFFFFFFF")) {
                PWLogger.d("未获取到模块的MAC地址");
                return false;
            } else {
                this.dceMac = mac;
                PWLogger.d("已获取到模块的MAC地址：" + this.dceMac);
                //已进入PDA配置模式，还未获取到有效的MAC地址
                if (EmptyUtils.isNotEmpty(this.listener)) {
                    this.listener.get().onDETMacChanged(this.dceMac);
                }
                return false;
            }
        } else {
            //已进入PDA配置模式，循环判断是否已经配置新的BE码
            String code = modules.getCode();
            if (EmptyUtils.isEmpty(code) || "BEFFFFFFFFFFFFFFFFFF".equals(code)) {
                PWLogger.d("模块的BE码未配置");
                return false;
            } else {
                PWLogger.d("模块的BE码已经配置完成：" + code);
                if (EmptyUtils.isNotEmpty(this.listener)) {
                    this.listener.get().onDeviceCodeChanged(code);
                }
                PWLogger.d("退出配置模式");
                this.quitConfigModel();
                return true;
            }
        }
    }

    private void checkUNPDAStartupModules(RSMSQueryModulesResponseEntity modules) {
        //未进入配置，判断BE码是否一致
        String code = null;
        if (EmptyUtils.isNotEmpty(this.listener)) {
            code = this.listener.get().findDeviceCode();
        }
        if (modules.getCode().equals(code)) {
            //一致：查询设备状态
            this.startQueryStatus();
            this.state = RSMS_STATE_RUNNING;
            PWLogger.d("本地BE码与模块参数一致，进入正常运行模式");
        } else {
            //不一致：进入PDA配置模式
            this.dceMac = null;
            PWLogger.d("本地BE码与模块参数不一致，进入PDA配置模式");
            this.enterPDAConfigModel();
        }
    }

    @Override
    public void onRSMSConnected() {
        this.pda = false;
        this.status = null;
        this.tasks.clear();
        this.lastTime = 0;
        this.dateTime = false;
        this.taskRuning = false;
        this.state = RSMS_STATE_STRTUP;
        PWLogger.d("连接成功查询模块参数，开启启动流程判断");
        this.queryModules();
    }

    @Override
    public void onRSMSException() {
        this.pda = false;
        this.status = null;
        this.tasks.clear();
        this.lastTime = 0;
        this.dateTime = false;
        this.taskRuning = false;
        this.state = RSMS_STATE_IDLE;
        this.stopQuertState();
        this.stopQueryModules();
        this.stopQueryDateTime();
        PWLogger.d("RSMS exception........");
    }

    @Override
    public void onRSMSStatusReceived(RSMSQueryStatusResponseEntity status) throws IOException {
        PWLogger.d("查询设备状态成功");
        this.status = status;
        //判断是用户查询还是自动查询，如果是用户查询需要调用接口反馈
        this.commandResponseReceived();
        if (!this.dateTime && this.isReady()) {
            this.dateTime = true;
            this.startQueryDateTime(0);
        }
        RSMSQueryStatusEntity entity = (RSMSQueryStatusEntity) sendBase;
        if (!entity.isFromUser()) {
            this.startQueryStatus();
        } else {
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onStatusReceived(status);
            }
        }
    }

    @Override
    public void onRSMSNetworkReceived(RSMSNetworkResponseEntity network) throws IOException {
        PWLogger.d("查询联网参数成功");
        if (EmptyUtils.isNotEmpty(this.listener)) {
            this.listener.get().onNetworkReceived(network);
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSModulesReceived(RSMSQueryModulesResponseEntity modules) throws IOException {
        PWLogger.d("查询模块参数成功");
        if (this.state == RSMS_STATE_STRTUP) {
            if (this.pda) {
                if (!this.checkPDAStartupModules(modules)) {
                    this.startQueryModules();
                }
            } else {
                this.checkUNPDAStartupModules(modules);
            }
        } else {
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onModulesReceived(modules);
            }
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) throws IOException {
        if (response.getConfigModel() == (byte) 0xB0) {
            this.dte = true;
            PWLogger.d("进入串口配置成功");
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onDTEConfigEntered();
            }
        } else {
            this.pda = true;
            this.clearCache();
            PWLogger.d("进入PDA配置成功,清空模块缓存数据");
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onPDAConfigEntered();
            }
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSQuitConfigReceived(RSMSResponseEntity response) throws IOException {
        if (this.pda) {
            this.pda = false;
            PWLogger.d("退出PDA配置成功");
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onPDAConfigQuited();
            }
        } else if (this.dte) {
            this.dte = false;
            PWLogger.d("退出DTE配置成功");
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onDTEConfigQuited();
            }
        }
        throw new IOException("Config model quited, need reconnect");
    }

    @Override
    public void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) throws IOException {
        RSMSCollectionEntity collection = (RSMSCollectionEntity) sendBase;
        if (RSMSTools.COLLECTION_DATE_TYPE == collection.getDataType()) {
            PWLogger.d("查询服务器时间指令发送成功");
        } else if (RSMSTools.COLLECTION_DATA_TYPE == collection.getDataType()) {
            PWLogger.d("设备数据采集成功");
            this.lastTime = System.nanoTime();
        } else if (RSMSTools.COLLECTION_EVENT_TYPE == collection.getDataType()) {
            PWLogger.d("用户操作日志采集成功");
        } else if (RSMSTools.COLLECTION_CONTROL_RESPONSE_TYPE == collection.getDataType()) {
            PWLogger.d("远程控制指令执行结果回复成功");
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSClearCacheReceived(RSMSResponseEntity response) throws IOException {
        PWLogger.d("清空本地缓存成功");
        if (this.state == RSMS_STATE_STRTUP) {
            this.startQueryModules();
        } else {
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onClearCacheSuccessed();
            }
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSRecoveryReceived(RSMSResponseEntity response) throws IOException {
        PWLogger.d("恢复出厂设置成功");
        this.quitConfigModel();
        if (EmptyUtils.isNotEmpty(this.listener)) {
            this.listener.get().onRecoverySuccessed();
        }
        //任务处理结束，重置taskRunning标志
        this.commandResponseReceived();
    }

    @Override
    public void onRSMSTransmissionReceived(RSMSTransmissionEntity entity) throws IOException {
        //回复透传信息已接收
        this.insertTask(new RSMSSendBaseEntity((short) RSMSTools.RSMS_TRANSMISSION_RESPONSE, false));
        switch (entity.getDataType()) {
            case RSMSTools.COLLECTION_DATE_TYPE:
                RSMSDateTimeEntity date = (RSMSDateTimeEntity) entity;
                if (EmptyUtils.isNotEmpty(this.listener)) {
                    this.startQueryDateTime(600);
                    this.listener.get().onDateTimeChanged(date.getTimestamp());
                }
                break;
            case RSMSTools.COLLECTION_CONTROL_COMMAND_TYPE:
                RSMSCommandEntity command = (RSMSCommandEntity) entity;
                int deviceType = command.getDeviceType();
                int controlCommand = command.getCommand();
                int protocolVersion = command.getProtocolVersion();
                PWLogger.d("接收到控制指令透传信息:{deviceType:" + deviceType + ", protocolVersion:" + protocolVersion + ",controlCommand:" + controlCommand + "}");
                if (EmptyUtils.isNotEmpty(this.listener)) {
                    if (this.listener.get().checkControlCommand(deviceType, protocolVersion, controlCommand)) {
                        this.listener.get().onControlCommandReceived(command);
                    } else {
                        RSMSCommandResponseEntity response = new RSMSCommandResponseEntity(command);
                        response.setHandleState((byte) 0x03);
                        this.insertTask(response);
                    }
                }
                break;
            default:
                PWLogger.d("接收到无法处理的透传信息");
                break;
        }
    }

    private class RSMSHandler extends Handler {
        public RSMSHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RSMS_INSERT_TASK_MSG:
                    RSMSSendBaseEntity send = (RSMSSendBaseEntity) msg.obj;
                    PWLogger.d("添加一个新的任务:" + RSMSTools.command2String(send.getCommandType()));
                    tasks.add(send);
                    sendEmptyMessage(RSMS_PROCESS_TASK_MSG);
                    break;
                case RSMS_PROCESS_TASK_MSG:
                    if (!isInitialized()) {
                        PWLogger.d("对象已销毁，无法处理，跳过....");
                        break;
                    }
                    if (state == RSMS_STATE_IDLE) {
                        PWLogger.d("串口连接中断，无法处理，跳过....");
                        break;
                    }
                    if (EmptyUtils.isEmpty(tasks)) {
                        PWLogger.d("队列为空，无需处理, 跳过....");
                        break;
                    }
                    if (taskRuning) {
                        break;
                    }
                    taskRuning = true;
                    sendBase = tasks.get(0);
                    PWLogger.d("开始处理指令：" + RSMSTools.command2String(sendBase.getCommandType()));
                    serialPort.sendCommand(sendBase);
                    if (!sendBase.isNeedResponse()) {
                        sendEmptyMessageDelayed(RSMS_FINISH_TASK_MSG, 100);
                    }
                    break;
                case RSMS_FINISH_TASK_MSG:
                    PWLogger.d("任务处理结束:" + RSMSTools.command2String(sendBase.getCommandType()));
                    taskRuning = false;
                    if (EmptyUtils.isNotEmpty(tasks)) {
                        tasks.remove(0);
                    }
                    sendEmptyMessage(RSMS_PROCESS_TASK_MSG);
                    break;
                case RSMS_QUERY_STATE_MSG:
                    if (state != RSMS_STATE_RUNNING) {
                        PWLogger.d("非运行模式，无需查询设备状态");
                        break;
                    }
                    String code = null;
                    if (EmptyUtils.isNotEmpty(listener)) {
                        code = listener.get().findDeviceCode();
                    }
                    RSMSQueryStatusEntity status = new RSMSQueryStatusEntity();
                    status.setMac(mac);
                    status.setFromUser(false);
                    status.setMcu(RSMSTools.DEFAULT_MAC);
                    status.setCode(RSMSTools.generateCode(code));
                    RSMSDTEManager.this.insertTask(status);
                    break;
                case RSMS_QUERY_MODULES_MSG:
                    if (state != RSMS_STATE_STRTUP) {
                        PWLogger.d("非启动模式，无需查询模块参数");
                        break;
                    }
                    RSMSDTEManager.this.queryModules();
                    break;
                case RSMS_QUERY_DATE_TIME_MSG:
                    if (state != RSMS_STATE_RUNNING) {
                        PWLogger.d("非运行模式，无法查询服务器时间");
                        break;
                    }
                    RSMSDTEManager.this.insertTask(new RSMSDateTimeCollectionEntity());
                    break;
            }
        }
    }
}
