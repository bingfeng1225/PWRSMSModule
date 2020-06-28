package cn.haier.bio.medical.rsms.serialport;


import java.io.IOException;
import java.lang.ref.WeakReference;

import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSTransmissionEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.serialport.listener.IRSMSListener;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwlogger.PWLogger;
import cn.qd.peiwen.serialport.PWSerialPortHelper;
import cn.qd.peiwen.serialport.PWSerialPortListener;
import cn.qd.peiwen.serialport.PWSerialPortState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSSerialPort implements PWSerialPortListener {
    private ByteBuf buffer;
    private PWSerialPortHelper helper;

    private boolean enabled = false;
    private WeakReference<IRSMSListener> listener;

    public RSMSSerialPort() {

    }

    public void init(String path) {
        createBuffer();
        createHelper(path);
    }

    public void enable() {
        if (this.isInitialized() && !this.enabled) {
            this.enabled = true;
            this.helper.open();
        }
    }

    public void disable() {
        if (this.isInitialized() && this.enabled) {
            this.enabled = false;
            this.helper.close();
        }
    }

    public void sendCommand(RSMSSendBaseEntity entity) {
        byte[] data = RSMSTools.packageCommand(entity);
        this.write(data);
    }

    public void release() {
        this.listener = null;
        this.destoryHelper();
        this.destoryBuffer();
    }

    public void changeListener(IRSMSListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    private boolean isInitialized() {
        if (this.helper == null) {
            return false;
        }
        if (this.buffer == null) {
            return false;
        }
        return true;
    }

    private void createBuffer() {
        if (this.buffer == null) {
            this.buffer = Unpooled.buffer(4);
        }
    }

    private void destoryBuffer() {
        if (null != this.buffer) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    private void createHelper(String path) {
        if (this.helper == null) {
            this.helper = new PWSerialPortHelper("RSMSSerialPort");
            this.helper.setPath(path);
            this.helper.setTimeout(10);
            this.helper.setBaudrate(9600);
            this.helper.init(this);
        }
    }

    private void destoryHelper() {
        if (null != this.helper) {
            this.helper.release();
            this.helper = null;
        }
    }

    private void write(byte[] data) {
        PWLogger.d("RSMS Send:" + RSMSTools.bytes2HexString(data, true, ", "));
        if (this.isInitialized() && this.enabled) {
            this.helper.write(data);
        }
    }

    @Override
    public void onConnected(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.buffer.clear();
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onRSMSConnected();
        }
    }

    @Override
    public void onException(PWSerialPortHelper helper,Throwable throwable) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (null != this.listener && null != this.listener.get()) {
            this.listener.get().onRSMSException(throwable);
        }
    }

    @Override
    public void onReadThreadReleased(PWSerialPortHelper helper) {

    }

    @Override
    public void onStateChanged(PWSerialPortHelper helper, PWSerialPortState state) {

    }

    @Override
    public void onByteReceived(PWSerialPortHelper helper, byte[] buffer, int length) throws IOException {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        this.buffer.writeBytes(buffer, 0, length);
        while (this.buffer.readableBytes() > 4) {
            //帧头监测
            int headerIndex = RSMSTools.indexOf(this.buffer, RSMSTools.HEADER);
            if (headerIndex == -1) {
                if (this.buffer.readableBytes() >= 256) {
                    byte[] data = new byte[this.buffer.readableBytes()];
                    this.buffer.readBytes(data, 0, data.length);
                    this.buffer.discardReadBytes();
                    PWLogger.d("缓冲区内的数据超过256，且不包含正常数据头，丢弃全部：" + RSMSTools.bytes2HexString(data));
                }
                break;
            }
            if (headerIndex > 0) {
                //抛弃帧头以前的数据
                byte[] data = new byte[headerIndex];
                this.buffer.readBytes(data, 0, headerIndex);
                this.buffer.discardReadBytes();
                PWLogger.d("丢弃帧头前不合法数据：" + RSMSTools.bytes2HexString(data));
                continue;
            }
            //长度监测
            //数据长度 = type(1) + cmd(1) + data(n) + check(1)
            //总长度 = header(2) + len(2) + data(len) + tailer(2)
            int len = this.buffer.getShort(2);
            if (this.buffer.readableBytes() < len + 6) {
                break;
            }
            //帧尾监测
            int tailerIndex = RSMSTools.indexOf(this.buffer, RSMSTools.TAILER);
            if (tailerIndex != len + 4) {
                //当前包尾位置错误 丢掉正常的包头以免重复判断
                this.buffer.skipBytes(2);
                this.buffer.discardReadBytes();
                PWLogger.d("帧尾位置不匹配，丢弃帧头，查找下一帧数据");
                continue;
            }
            this.buffer.markReaderIndex();
            byte[] data = new byte[len + 6];
            this.buffer.readBytes(data, 0, data.length);
            //校验和检验
            if (!RSMSTools.checkFrame(data)) {
                this.buffer.resetReaderIndex();
                this.buffer.skipBytes(2);
                this.buffer.discardReadBytes();
                PWLogger.d("校验和不匹配，丢弃帧头，查找下一帧数据");
                continue;
            }
            int type = this.buffer.getShort(4);
            this.buffer.discardReadBytes();
            PWLogger.d("RSMSSerialPort Recv:" + RSMSTools.bytes2HexString(data, true, ", "));
            switch (type) {
                case RSMSTools.RSMS_RESPONSE_QUERY_STATUS: {
                    RSMSQueryStatusResponseEntity entity = RSMSTools.parseRSMSStatusEntity(data);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSStatusReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_QUERY_NETWORK: {
                    RSMSNetworkResponseEntity entity = RSMSTools.parseRSMSNetworkEntity(data);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSNetworkReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_QUERY_MODULES: {
                    RSMSQueryModulesResponseEntity entity = RSMSTools.parseRSMSModulesEntity(data);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSModulesReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_ENTER_CONFIG: {
                    RSMSEnterConfigResponseEntity entity = RSMSTools.parseRSMSConfigModelResponseEntity(data);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSEnterConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_QUIT: {
                    RSMSResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSQuitConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_DTE_MODEL: {
                    RSMSResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSDTEModelConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_RECOVERY: {
                    RSMSResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSRecoveryReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_CLEAR_CACHE: {
                    RSMSResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSClearCacheReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_COLLECTION_DATA: {
                    RSMSRecvBaseEntity entity = new RSMSRecvBaseEntity();
                    entity.setCommandType(type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSDataCollectionReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_TRANSMISSION_COMMAND: {
                    RSMSTransmissionEntity entity = RSMSTools.parseRSMSTransmissionEntity(data);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSTransmissionReceived(entity);
                    }
                    break;
                }
                default:
                    byte[] bytes = RSMSTools.short2Bytes((short) type);
                    if (null != this.listener && null != this.listener.get()) {
                        this.listener.get().onRSMSPrint("RSMSSerialPort 指令" + RSMSTools.bytes2HexString(bytes, true) + "暂不支持");
                    }
                    break;
            }
        }
    }
}
