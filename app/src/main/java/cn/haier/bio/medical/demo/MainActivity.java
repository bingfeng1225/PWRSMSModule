package cn.haier.bio.medical.demo;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import cn.haier.bio.medical.demo.control.CommandTools;
import cn.haier.bio.medical.demo.control.recv.TemptureCommandEntity;
import cn.haier.bio.medical.demo.control.send.LTBCollectionEntity;
import cn.haier.bio.medical.demo.control.send.TemptureResonseEntity;
import cn.haier.bio.medical.ltb.ILTBListener;
import cn.haier.bio.medical.ltb.LTBManager;
import cn.haier.bio.medical.ltb.entity.LTBDataEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSNetworkResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryModulesResponseEntity;
import cn.haier.bio.medical.rsms.entity.recv.RSMSQueryStatusResponseEntity;
import cn.haier.bio.medical.rsms.IRSMSDTEListener;
import cn.haier.bio.medical.rsms.entity.recv.server.RSMSCommandEntity;
import cn.haier.bio.medical.rsms.RSMSDTEManager;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSCommandResponseEntity;
import cn.haier.bio.medical.rsms.entity.send.client.RSMSOperationCollectionEntity;
import cn.haier.bio.medical.rsms.tools.RSMSTools;
import cn.qd.peiwen.pwlogger.PWLogger;
import cn.qd.peiwen.pwtools.ByteUtils;
import cn.qd.peiwen.pwtools.EmptyUtils;
import cn.qd.peiwen.serialport.PWSerialPort;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class MainActivity extends AppCompatActivity implements IRSMSDTEListener, ILTBListener {
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

        ByteBuf buf = Unpooled.buffer();


        int xx = RSMSTools.RSMS_TRANSMISSION_COMMAND;
        buf.writeShortLE(xx);

        buf.markReaderIndex();

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        PWLogger.e("data = " + ByteUtils.bytes2HexString(data));

        buf.resetReaderIndex();

        PWLogger.e("xx = " + xx);
        PWLogger.e("xxx = " + (short)xx);
        PWLogger.e("xxxx = " + buf.readShortLE());
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
    public void onControlCommandReceived(RSMSCommandEntity command) {
        switch (command.getCommand()){
            case CommandTools.CONTROL_TEMPERATURE_COMMAND:
                TemptureCommandEntity entity = CommandTools.parseTemptureCommandEntity(command.getControl());
                TemptureResonseEntity response = new TemptureResonseEntity(command);
                if(EmptyUtils.isEmpty(entity)){
                    response.setHandleState((byte)0x02);
                }else{
                    response.setHandleState((byte)0x01);
                }
                RSMSDTEManager.getInstance().collectionDeviceData(response);
                break;
            default:

                break;
        }
    }

    @Override
    public boolean checkControlCommand(int deviceType, int protocolVersion, int controlCommand) {
        return CommandTools.checkControlCommand(deviceType,protocolVersion,controlCommand);
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
