package cn.haier.bio.medical.demo;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import cn.haier.bio.medical.demo.control.ControlTools;
import cn.haier.bio.medical.demo.control.recv.TemptureEntity;
import cn.haier.bio.medical.demo.control.send.LTBCollectionEntity;
import cn.haier.bio.medical.ltb.ILTBListener;
import cn.haier.bio.medical.ltb.LTBManager;
import cn.haier.bio.medical.ltb.entity.LTBDataEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSCommontResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSControlCommandEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSEnterConfigResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryPDAModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSRecvBaseEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSAModelConfigEntity;
import cn.haier.bio.medical.rsms.entity.send.RSMSDTEModelConfigEntity;
import cn.haier.bio.medical.rsms.listener.IRSMSDTEListener;
import cn.haier.bio.medical.rsms.listener.IRSMSListener;
import cn.haier.bio.medical.rsms.serialport.RSMSCommandManager;
import cn.haier.bio.medical.rsms.serialport.RSMSDTEManager;
import cn.qd.peiwen.pwtools.EmptyUtils;
import cn.qd.peiwen.serialport.PWSerialPort;


public class MainActivity extends AppCompatActivity implements IRSMSListener, IRSMSDTEListener, ILTBListener {
    private String code;
    private LTBDataEntity entity;

    private TextView textView;
    private QRCodeDialog qrCodeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textView = findViewById(R.id.text);
        this.textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        String path = "/dev/ttyS5";
        if (!"magton".equals(Build.MODEL)) {
            path = "/dev/ttyS1";
        }

        this.code = this.getSharedPreferences("Demo", 0)
                .getString("DEV_CODE", null);
//        RSMSCommandManager.getInstance().init(path,this);
//        RSMSCommandManager.getInstance().enable();

        RSMSDTEManager.getInstance().init(path);
        RSMSDTEManager.getInstance().changeListener(this);
        RSMSDTEManager.getInstance().enable();

        path = "/dev/ttyS2";
        if (!"magton".equals(Build.MODEL)) {
            path = "/dev/ttyS4";
        }

        LTBManager.getInstance().init(path);
        LTBManager.getInstance().changeListener(this);
        LTBManager.getInstance().enable();
    }

    private void refreshTextView(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int lineCount = textView.getLineCount();
                if (lineCount > 150) {
                    textView.setText("");
                }
                StringBuffer buffer = new StringBuffer(textView.getText());
                buffer.append(text);
                textView.setText(buffer.toString());
                int offset = textView.getLineCount() * textView.getLineHeight();
                if (offset > textView.getHeight()) {
                    textView.scrollTo(0, offset - textView.getHeight());
                } else {
                    textView.scrollTo(0, 0);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RSMSCommandManager.getInstance().disable();
        RSMSCommandManager.getInstance().release();
        RSMSDTEManager.getInstance().disable();
        RSMSDTEManager.getInstance().release();
    }

    public void onClicked(View view) {
        switch (view.getId()) {
            case R.id.query_status:
                RSMSDTEManager.getInstance().queryStatus();
                break;
            case R.id.query_network:
                RSMSDTEManager.getInstance().queryNetwork();
                break;
            case R.id.query_modules:
                RSMSDTEManager.getInstance().queryModules();
                break;
            case R.id.query_pda_modules:
                RSMSCommandManager.getInstance().queryPDAModules();
                break;
            case R.id.quit_config:
                RSMSCommandManager.getInstance().quitConfigModel();
                break;
            case R.id.clear_cache:
                RSMSDTEManager.getInstance().clearCache();
                break;
            case R.id.recovery:
                RSMSDTEManager.getInstance().recovery();
                break;
            case R.id.enter_dce_config:
                RSMSCommandManager.getInstance().enterDTEConfigModel();
                break;
            case R.id.enter_pda_config:
                RSMSCommandManager.getInstance().enterPDAConfigModel();
                break;
            case R.id.clear:
                textView.setText("");
                break;
            case R.id.config_network1: {
                RSMSDTEModelConfigEntity entity = new RSMSDTEModelConfigEntity();
                entity.setModel((byte) 0x01);
                entity.setAddress("msg.haierbiomedical.com");
                entity.setPort("1777");
                RSMSCommandManager.getInstance().configDTEModel(entity);
                break;
            }
            case R.id.config_network2: {
                RSMSDTEModelConfigEntity entity = new RSMSDTEModelConfigEntity();
                entity.setModel((byte) 0x02);
                entity.setAddress("msg.haierbiomedical.com");
                entity.setPort("1777");
                entity.setWifiName("Bio_Wireless");
                entity.setWifiPassword("12345678");
                entity.setApnName("");
                entity.setApnPassword("");
                RSMSCommandManager.getInstance().configDTEModel(entity);
                break;
            }
            case R.id.config_network3: {
                RSMSDTEModelConfigEntity entity = new RSMSDTEModelConfigEntity();
                entity.setModel((byte) 0x03);
                entity.setAddress("msg.haierbiomedical.com");
                entity.setPort("1777");
                entity.setWifiName("Bio_Wireless");
                entity.setWifiPassword("12345678");
                entity.setApnName("");
                entity.setApnPassword("");
                RSMSCommandManager.getInstance().configDTEModel(entity);
                break;
            }
            case R.id.config_becode: {
                RSMSAModelConfigEntity entity = new RSMSAModelConfigEntity();
                entity.setCode("BE0F0P01T00QGJ190004");
                entity.setUsername("haier");
                entity.setPassword("1234");
                RSMSCommandManager.getInstance().configAModel(entity);
                break;
            }
            case R.id.collection:
//                byte[] data = {
//                        (byte) 0x85, (byte) 0xFE, (byte) 0x31, (byte) 0xF8, (byte) 0xD8,
//                        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xCE, (byte) 0xFF,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0xDC, (byte) 0xFF, (byte) 0xDC, (byte) 0xFF, (byte) 0xDC,
//                        (byte) 0xFF, (byte) 0xDC, (byte) 0xFF, (byte) 0xDC, (byte) 0xFF,
//                        (byte) 0xDC, (byte) 0xFF, (byte) 0xDC, (byte) 0xFF, (byte) 0xDC,
//                        (byte) 0xFF, (byte) 0xDC, (byte) 0xFF, (byte) 0xDC, (byte) 0xFF,
//                        (byte) 0x24, (byte) 0xFA, (byte) 0x88, (byte) 0xFA, (byte) 0xC0,
//                        (byte) 0xF9, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
//                        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
//                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
//                };
//                RSMSCommandManager.getInstance().collectionDeviceData(new TestSendEntity(data));
                break;
        }
    }

    @Override
    public void onRSMSConnected() {
        this.refreshTextView("模块连接成功\n");
    }

    @Override
    public void onRSMSException() {
        this.refreshTextView("模块连接断开，三秒后重新连接:\n");
    }

    @Override
    public void onMessageSended(String data) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("发送数据:\n");
        buffer.append(data + "\n");
        this.refreshTextView(buffer.toString());
    }

    @Override
    public void onMessageRecved(String data) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("接收数据:\n");
        buffer.append(data + "\n");
        this.refreshTextView(buffer.toString());
    }

    @Override
    public void onControlReceived(RSMSControlCommandEntity entity) {

    }

    @Override
    public void onRSMSStatusReceived(RSMSQueryStatusResponseEntity status) {
        this.refreshTextView(status.toString());
    }

    @Override
    public void onRSMSNetworkReceived(RSMSNetworkResponseEntity network) {
        this.refreshTextView(network.toString());
    }


    @Override
    public void onRSMSModulesReceived(RSMSQueryModulesResponseEntity modules) {
        this.refreshTextView(modules.toString());
    }

    @Override
    public void onRSMSPDAModulesReceived(RSMSQueryPDAModulesResponseEntity modules) {
        this.refreshTextView(modules.toString());
    }

    @Override
    public void onRSMSUnknownReceived() {
        this.refreshTextView("接收到未知类型的信息\n");
    }

    @Override
    public void onRSMSDataCollectionReceived(RSMSRecvBaseEntity entity) {
        this.refreshTextView("数据采集成功\n");
    }

    @Override
    public void onRSMSControlReceived(RSMSControlCommandEntity entity) {
        switch (entity.getCommand()) {
            case (short) 0x90FE:
                TemptureEntity tempture = ControlTools.parseTemptureEntity(entity.getControl());
                this.refreshTextView("收到控制温度指令，设置温度为：" + tempture.getTempture() + "\n");
                break;
            default:
                break;
        }
    }

    @Override
    public void onRSMSRecoveryReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("恢复出厂设置成功\n");
        } else {
            this.refreshTextView("恢复出厂设置失败\n");
        }
    }

    @Override
    public void onRSMSClearCacheReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("清空本地缓存成功\n");
        } else {
            this.refreshTextView("清空本地缓存失败\n");
        }
    }

    @Override
    public void onRSMSQuitConfigReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("退出配置模式成功\n");
        } else {
            this.refreshTextView("退出配置模式失败\n");
        }
    }

    @Override
    public void onRSMSAModelConfigReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("PDA机编配置成功\n");
        } else {
            this.refreshTextView("PDA机编配置失败\n");
        }
    }

    @Override
    public void onRSMSBModelConfigReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("PDA配置网络参数成功\n");
        } else {
            this.refreshTextView("PDA配置网络参数失败\n");
        }
    }

    @Override
    public void onRSMSDTEModelConfigReceived(RSMSCommontResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("DTE配置网络参数成功\n");
        } else {
            this.refreshTextView("DTE配置网络参数失败\n");
        }
    }

    @Override
    public void onRSMSEnterConfigReceived(RSMSEnterConfigResponseEntity response) {
        if (0x01 == response.getResponse()) {
            this.refreshTextView("进入" + (((byte) 0xB0 == response.getConfigModel()) ? "串口" : "PDA") + "配置模式成功\n");
        } else {
            this.refreshTextView("进入" + (((byte) 0xB0 == response.getConfigModel()) ? "串口" : "PDA") + "配置模式失败\n");
        }
    }


    @Override
    public String findDeviceCode() {
        return this.code;
    }

    @Override
    public void onPDAConfigQuited() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissQRCodeDialog();
            }
        });
    }

    @Override
    public void onDTEConfigQuited() {

    }

    @Override
    public void onDTEConfigEntered() {

    }

    @Override
    public void onLTBSwitchWriteModel() {
        if (!"magton".equals(Build.MODEL)) {
            PWSerialPort.writeFile("/sys/class/gpio/gpio24/value", "0");
        } else {
            PWSerialPort.writeFile("/sys/class/misc/sunxi-acc/acc/sochip_acc", "1");
        }
    }

    @Override
    public void onLTBSwitchReadModel() {
        if (!"magton".equals(Build.MODEL)) {
            PWSerialPort.writeFile("/sys/class/gpio/gpio24/value", "1");
        } else {
            PWSerialPort.writeFile("/sys/class/misc/sunxi-acc/acc/sochip_acc", "0");
        }
    }

    @Override
    public void onPDAConfigEntered() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showQRCodeDialog();
            }
        });
    }

    @Override
    public void onRecoverySuccessed() {

    }

    @Override
    public void onClearCacheSuccessed() {

    }

    @Override
    public void onDETMacChanged(String mac) {
        if (EmptyUtils.isNotEmpty(this.qrCodeDialog)) {
            this.qrCodeDialog.changeMac(mac);
        }
    }

    @Override
    public void onDeviceCodeChanged(String code) {
        this.code = code;
        SharedPreferences sp = this.getSharedPreferences("Demo", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("DEV_CODE", this.code);
        editor.commit();
    }

    @Override
    public void onNetworkReceived(RSMSNetworkResponseEntity network) {
        this.refreshTextView(network.toString());
    }

    @Override
    public void onStatusReceived(RSMSQueryStatusResponseEntity status) {
        this.refreshTextView(status.toString());
    }

    @Override
    public void onModulesReceived(RSMSQueryModulesResponseEntity modules) {
        this.refreshTextView(modules.toString());
    }

    private void showQRCodeDialog() {
        if (EmptyUtils.isEmpty(this.qrCodeDialog)) {
            this.qrCodeDialog = new QRCodeDialog(this);
        }
        if (!this.qrCodeDialog.isShowing()) {
            this.qrCodeDialog.show();
        }
    }

    private void dismissQRCodeDialog() {
        if (EmptyUtils.isNotEmpty(this.qrCodeDialog)) {
            this.qrCodeDialog.dismiss();
            this.qrCodeDialog = null;
        }
    }


    @Override
    public void onLTBReady() {

    }

    @Override
    public void onLTBConnected() {

    }

    @Override
    public void onLTBException() {

    }

    @Override
    public boolean onLTBSystemChanged(int type) {
        return false;
    }

    @Override
    public byte[] packageLTBResponse(int type) {
        return new byte[0];
    }

    @Override
    public void onLTBStateChanged(LTBDataEntity entity) {
        LTBDataEntity temp = this.entity;
        this.entity = entity;

        RSMSDTEManager manager = RSMSDTEManager.getInstance();
        boolean ready = manager.isReady();
        boolean overtime = manager.isArrivalTime();
        boolean changed = (!entity.isStatusEquals(temp) || !entity.isAlarmsEquals(temp));
        if (ready && (overtime || changed)) {
            LTBCollectionEntity collection = new LTBCollectionEntity();
            collection.setEntity(entity);
            manager.collectionDeviceData(collection);
        }
        this.entity = entity;
    }
}
