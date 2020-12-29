package cn.haier.bio.medical.demo;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import cn.haier.bio.medical.demo.control.CommandTools;
import cn.haier.bio.medical.demo.control.recv.TemptureCommandEntity;
import cn.haier.bio.medical.demo.control.send.TemptureResonseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.IRSMSDTEListener;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.RSMSDTEManager;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSOperationCollectionEntity;
import cn.qd.peiwen.logger.PWLogger;
import cn.qd.peiwen.pwtools.ByteUtils;
import cn.qd.peiwen.pwtools.EmptyUtils;


public class MainActivity extends AppCompatActivity implements IRSMSDTEListener {
    private String code;

    private TextView textView;
    private QRCodeDialog qrCodeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textView = findViewById(R.id.text);
        this.textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        String path = "/dev/ttyS4";
        if (!"magton".equals(Build.MODEL)) {
            path = "/dev/ttyS1";
        }

        this.code = this.getSharedPreferences("Demo", 0)
                .getString("DEV_CODE", null);

        RSMSDTEManager.getInstance().init(path);
        RSMSDTEManager.getInstance().changeListener(this);
        RSMSDTEManager.getInstance().enable();
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
            case R.id.quit_config:
                RSMSDTEManager.getInstance().quitConfigModel();
                break;
            case R.id.clear_cache:
                RSMSDTEManager.getInstance().clearCache();
                break;
            case R.id.recovery:
                RSMSDTEManager.getInstance().recovery();
                break;
            case R.id.enter_dce_config:
                RSMSOperationCollectionEntity operation = new RSMSOperationCollectionEntity();
                operation.setDeviceType(CommandTools.DEVICE_TYPE);
                operation.setProtocolVersion(CommandTools.PROTOCOL_VERSION);
                operation.setOperation(0x01);
                RSMSDTEManager.getInstance().collectionDeviceData(operation);
                break;
            case R.id.clear:
                textView.setText("");
                break;
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
    public void onDTEReady() {

    }

    @Override
    public void onDTEPrint(String message) {
        PWLogger.debug("" + message);
    }

    @Override
    public void onDTEException(Throwable throwable) {
        PWLogger.error(throwable);
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
    public void onControlCommandReceived(RSMSCommandEntity command) {
        switch (command.getCommand()) {
            case CommandTools.CONTROL_TEMPERATURE_COMMAND:
                TemptureCommandEntity entity = CommandTools.parseTemptureCommandEntity(command.getControl());
                TemptureResonseEntity response = new TemptureResonseEntity(command);
                if (EmptyUtils.isEmpty(entity)) {
                    response.setHandleState((byte) 0x02);
                } else {
                    response.setHandleState((byte) 0x01);
                }
                RSMSDTEManager.getInstance().collectionDeviceData(response);
                break;
            default:

                break;
        }
    }

    @Override
    public void onDateTimeChanged(long time) {
        PWLogger.debug("RSMS DateTime:" + new Date(time));
    }

    @Override
    public boolean checkControlCommand(int deviceType, int protocolVersion, int controlCommand) {
        return CommandTools.checkControlCommand(deviceType, protocolVersion, controlCommand);
    }


    @Override
    public void onNetworkReceived(RSMSNetworkResponseEntity network) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("联网模式：");
        int model = network.getModel();
        if (model == 0x01) {
            buffer.append("4G\n");
        } else if (model == 0x02) {
            buffer.append("Wifi\n");
        } else {
            buffer.append("Auto\n");
        }
        buffer.append("服务器地址：" + network.getAddress() + ":" + network.getPort() + "\n");
        if (!EmptyUtils.isEmpty(network.getWifiName())) {
            buffer.append("Wifi名称：" + network.getWifiName() + "\n");
            buffer.append("Wifi密码：" + network.getWifiPassword() + "\n");
        }

        if (!EmptyUtils.isEmpty(network.getApn())) {
            buffer.append("APN服务：" + network.getApn() + "\n");
        }
        this.refreshTextView(buffer.toString());
    }

    @Override
    public void onStatusReceived(RSMSQueryStatusResponseEntity status) {
        StringBuilder builder = new StringBuilder();
        builder.append("联网模式：");
        int model = status.getModel();
        if (model == 0x01) {
            builder.append("4G\n");
        } else if (model == 0x02) {
            builder.append("Wifi\n");
        } else {
            builder.append("Auto\n");
        }
        //状态指示
        byte state = status.getStatus();
        builder.append("Sim卡接入状态：");
        if ((state & 0x04) == 0x04) {
            builder.append("失败\n");
        } else {
            builder.append("成功\n");
        }

        builder.append("4G联网状态：");
        if ((state & 0x02) == 0x02) {
            builder.append("失败\n");
        } else {
            builder.append("成功\n");
        }

        builder.append("4G信号强度：" + status.getLevel() + "\n");

        builder.append("4G连接云平台状态：");
        if ((state & 0x01) == 0x01) {
            builder.append("成功\n");
        } else {
            builder.append("失败\n");
        }

        builder.append("Wifi检索状态：");
        if ((state & 0x20) == 0x20) {
            builder.append("失败\n");
        } else {
            builder.append("成功\n");
        }
        builder.append("Wifi认证状态：");
        if ((state & 0x10) == 0x10) {
            builder.append("失败\n");
        } else {
            builder.append("成功\n");
        }
        builder.append("Wifi信号强度：" + status.getWifiLevel() + "\n");
        builder.append("Wifi连接云平台状态：");
        if ((state & 0x08) == 0x08) {
            builder.append("成功\n");
        } else {
            builder.append("失败\n");
        }

        builder.append("模块准备状态：");
        if ((state & 0x80) == 0x80) {
            builder.append("成功\n");
        } else {
            builder.append("失败\n");
        }
        builder.append("数据上传频率：" + status.getUploadFrequency() + "\n");
        builder.append("数据采集频率：" + status.getAcquisitionFrequency() + "\n");
        this.refreshTextView(builder.toString());
    }

    @Override
    public void onModulesReceived(RSMSQueryModulesResponseEntity modules) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("MCU：" + ByteUtils.bytes2HexString(modules.getMcu()) + "\n");
        buffer.append("MAC：" + ByteUtils.bytes2HexString(modules.getMac()) + "\n");
        buffer.append("机编：" + modules.getCode() + "\n");
        buffer.append("IMEI：" + modules.getImei() + "\n");
        buffer.append("ICCID：" + modules.getIccid() + "\n");
        buffer.append("SIM卡号码：" + modules.getPhone() + "\n");
        buffer.append("运营商：" + modules.getOperator() + "\n");
        buffer.append("MCU版本：" + modules.getMcuVersion() + "\n");
        buffer.append("Wifi版本：" + modules.getWifiVersion() + "\n");
        buffer.append("软件版本：" + modules.getModuleVersion() + "\n");
        this.refreshTextView(buffer.toString());
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
}
