package cn.haier.bio.medical.rsms.serialport;


import java.io.IOException;
import java.lang.ref.WeakReference;

import cn.haier.bio.medical.rsms.entity.recv.RSMSCommontResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSControlCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryPDAModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSSendBaseEntity;
import cn.haier.bio.medical.rsms.listener.IRSMSListener;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwlogger.PWLogger;
import cn.qd.peiwen.pwtools.ByteUtils;
import cn.qd.peiwen.pwtools.EmptyUtils;
import cn.qd.peiwen.serialport.PWSerialPortHelper;
import cn.qd.peiwen.serialport.PWSerialPortListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RSMSSerialPort implements PWSerialPortListener {
    private ByteBuf buffer;
    private PWSerialPortHelper helper;

    private String path;
    private boolean enabled = false;
    private WeakReference<IRSMSListener> listener;

    public RSMSSerialPort() {

    }

    public void init(String path) {
        this.path = path;
        createBuffer();
        createHelper();
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
        if (EmptyUtils.isEmpty(this.helper)) {
            return false;
        }
        if (EmptyUtils.isEmpty(this.buffer)) {
            return false;
        }
        return true;
    }

    private void createBuffer() {
        if (EmptyUtils.isEmpty(this.buffer)) {
            this.buffer = Unpooled.buffer(4);
        }
    }

    private void destoryBuffer() {
        if (EmptyUtils.isNotEmpty(this.buffer)) {
            this.buffer.release();
            this.buffer = null;
        }
    }

    private void createHelper() {
        if (EmptyUtils.isEmpty(this.helper)) {
            this.helper = new PWSerialPortHelper("RSMSSerialPort");
            this.helper.setPath(path);
            this.helper.setTimeout(10);
            this.helper.setBaudrate(115200);
            this.helper.init(this);
        }
    }

    private void destoryHelper() {
        if (EmptyUtils.isNotEmpty(this.helper)) {
            this.helper.release();
            this.helper = null;
        }
    }

    private void write(byte[] data) {
        String log = ByteUtils.bytes2HexString(data, true, ", ");
        PWLogger.d("RSMS Send:" + log);
        if (EmptyUtils.isNotEmpty(this.listener)) {
            this.listener.get().onMessageSended(log);
        }
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
        if (EmptyUtils.isNotEmpty(this.listener)) {
            this.listener.get().onRSMSConnected();
        }
    }

    @Override
    public void onException(PWSerialPortHelper helper) {
        if (!this.isInitialized() || !helper.equals(this.helper)) {
            return;
        }
        if (EmptyUtils.isNotEmpty(this.listener)) {
            this.listener.get().onRSMSException();
        }
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
                    PWLogger.d("缓冲区内的数据超过256，且不包含正常数据头，丢弃全部：" + ByteUtils.bytes2HexString(data));
                }
                break;
            }
            if (headerIndex > 0) {
                //抛弃帧头以前的数据
                byte[] data = new byte[headerIndex];
                this.buffer.readBytes(data, 0, headerIndex);
                this.buffer.discardReadBytes();
                PWLogger.d("丢弃帧头前不合法数据：" + ByteUtils.bytes2HexString(data));
                continue;
            }
            //长度监测
            //数据长度 = type(1) + cmd(1) + data(n) + check(1)
            //总长度 = header(2) + len(2) + data(len) + tailer(2)
            short len = this.buffer.getShort(2);
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
            short type = this.buffer.getShort(4);
            this.buffer.discardReadBytes();
            String log = ByteUtils.bytes2HexString(data, true, ", ");
            PWLogger.d("RSMS Recv:" + log);
            if (EmptyUtils.isNotEmpty(this.listener)) {
                this.listener.get().onMessageRecved(log);
            }
            switch (type) {
                case RSMSTools.RSMS_RESPONSE_QUERY_STATUS: {
                    RSMSQueryStatusResponseEntity entity = RSMSTools.parseRSMSStatusEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSStatusReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_QUERY_NETWORK: {
                    RSMSNetworkResponseEntity entity = RSMSTools.parseRSMSNetworkEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSNetworkReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_QUERY_MODULES: {
                    RSMSQueryModulesResponseEntity entity = RSMSTools.parseRSMSModulesEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSModulesReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_QUERY_PDA_MODULES: {
                    RSMSQueryPDAModulesResponseEntity entity = RSMSTools.parseRSMSPDAModulesEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSPDAModulesReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_ENTER_CONFIG: {
                    RSMSEnterConfigResponseEntity entity = RSMSTools.parseRSMSConfigModelResponseEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSEnterConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_QUIT: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSQuitConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_DTE_MODEL: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSDTEModelConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_A_MODEL: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSAModelConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_B_MODEL: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSBModelConfigReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_RECOVERY: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSRecoveryReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_CONFIG_CLEAR_CACHE: {
                    RSMSCommontResponseEntity entity = RSMSTools.parseRSMSResponseEntity(data);
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSClearCacheReceived(entity);
                    }
                    break;
                }
                case RSMSTools.RSMS_RESPONSE_COLLECTION_DATA: {
                    RSMSRecvBaseEntity entity = new RSMSRecvBaseEntity();
                    entity.setCommandType(type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSDataCollectionReceived(entity);
                    }
                    break;
                }
                case (short) RSMSTools.RSMS_CONTROL_COMMAND: {
                    RSMSControlCommandEntity entity = RSMSTools.parseRSMSControlEntity(data);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSControlReceived(entity);
                    }
                    break;
                }
                default:
                    byte[] bytes = ByteUtils.short2Bytes((short) type);
                    if (EmptyUtils.isNotEmpty(this.listener)) {
                        this.listener.get().onRSMSUnknownReceived();
                    }
                    PWLogger.d("指令" + ByteUtils.bytes2HexString(bytes, true) + "暂不支持");
                    break;
            }
        }
    }

}
