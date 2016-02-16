package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.LayerDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataHandler;

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "CarduinoStatusActivity";

    private boolean onExit = false;

    private final Handler screensaverHandler = new Handler();

    private View      statusActivityView;

    //main toolbar
    private Toolbar   topToolbar;
    private ImageView imageViewExit;
    private ImageView imageViewSettings;


    private ImageView imageViewDeviceRemoteIp;
    private TextView  textViewDeviceRemoteIpName;
    private TextView  textViewDeviceRemoteIp;

    private ImageView imageViewIpConnection;
    private TextView  textViewIpConnection;
    private TextView  textViewIpConnectionStatus;
    private TextView  textViewIpConnectionError;

    private ImageView imageViewDeviceTransceiver;
    private TextView  textViewDeviceTransceiverIpName;
    private TextView  textViewDeviceTransceiverIp;
    private ImageView imageViewSettingsTransceiver;

    private ImageView imageViewSerialConnection;
    private TextView  textViewSerialConnection;
    private TextView  textViewSerialConnectionStatus;
    private TextView  textViewSerialConnectionError;
    private ImageView imageViewSettingsBluetooth;

    private ImageView imageViewDeviceArduino;
    private TextView  textViewDeviceArduinoName;
    private TextView  textViewDeviceArduino;

    //switch mode toolbar
    private ImageView imageViewSwitchModePrev;
    private ImageView imageViewSwitchModeNext;
    private TextView  textviewSwitchMode;
    //druve button toolbar
    private Toolbar driveButton;

    private CarduinodroidApplication carduino;

    private IntentFilter serialStatusChangeFilter;
    private SerialStatusActivityStatusChangeReceiver serialStatusChangeReceiver;
    private IntentFilter ipStatusChangeFilter;
    private IpStatusActivityStatusChangeReceiver ipStatusChangeReceiver;
    private UsbBroadcastReciever usbReciever;
    private IntentFilter usbFilter;

    private CarduinoData getData(){
        return carduino.dataHandler.getData();
    }
    private CarduinoDroidData getDData(){
            return carduino.dataHandler.getDData();
    }
    private DataHandler getDataHandler(){
        return carduino.dataHandler;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.carduino = (CarduinodroidApplication) getApplication();
        setContentView(R.layout.activity_status);
        // prevent the application from switching to landscape-mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        serialStatusChangeReceiver =  new SerialStatusActivityStatusChangeReceiver();
        serialStatusChangeFilter =    new IntentFilter(Constants.EVENT.SERIAL_STATUS_CHANGED);

        ipStatusChangeReceiver =  new IpStatusActivityStatusChangeReceiver();
        ipStatusChangeFilter =    new IntentFilter(Constants.EVENT.IP_STATUS_CHANGED);

        usbReciever = new UsbBroadcastReciever();
        usbFilter = new IntentFilter(Constants.PERMISSION.USB);
        registerReceiver(usbReciever, usbFilter);

        //get the Views
        statusActivityView =                (View     ) findViewById(R.id.statusActivityView);
        topToolbar =                        (Toolbar  ) findViewById(R.id.topToolbar);
        imageViewExit =                     (ImageView) findViewById(R.id.imageViewExit);
        imageViewSettings =                 (ImageView) findViewById(R.id.imageViewSettings);

        imageViewDeviceRemoteIp =           (ImageView) findViewById(R.id.imageViewDeviceRemoteIp);
        textViewDeviceRemoteIpName =        (TextView ) findViewById(R.id.textViewDeviceRemoteIpName);
        textViewDeviceRemoteIp =            (TextView ) findViewById(R.id.textViewDeviceRemoteIp);

        imageViewIpConnection =             (ImageView) findViewById(R.id.imageViewIpConnection);
        textViewIpConnection =              (TextView ) findViewById(R.id.textViewIpConnection);
        textViewIpConnectionStatus =        (TextView ) findViewById(R.id.textViewIpConnectionStatus);
        textViewIpConnectionError =         (TextView ) findViewById(R.id.textViewIpConnectionError);

        imageViewDeviceTransceiver =        (ImageView) findViewById(R.id.imageViewDeviceTransceiver);
        textViewDeviceTransceiverIpName =   (TextView ) findViewById(R.id.textViewDeviceTransceiverIpName);
        textViewDeviceTransceiverIp =       (TextView ) findViewById(R.id.textViewDeviceTransceiverIp);
        imageViewSettingsTransceiver =      (ImageView) findViewById(R.id.imageViewSettingsTransceiver);

        imageViewSerialConnection =         (ImageView) findViewById(R.id.imageViewSerialConnection);
        textViewSerialConnection =          (TextView ) findViewById(R.id.textViewSerialConnection);
        textViewSerialConnectionStatus =    (TextView ) findViewById(R.id.textViewSerialConnectionStatus);
        textViewSerialConnectionError =     (TextView ) findViewById(R.id.textViewSerialConnectionError);
        imageViewSettingsBluetooth =        (ImageView) findViewById(R.id.imageViewSettingsBluetooth);

        imageViewDeviceArduino =            (ImageView) findViewById(R.id.imageViewDeviceArduino);
        textViewDeviceArduinoName =         (TextView ) findViewById(R.id.textViewDeviceArduinoName);
        textViewDeviceArduino =             (TextView ) findViewById(R.id.textViewDeviceArduino);
        //mode switch toolbar
        imageViewSwitchModePrev =           (ImageView) findViewById(R.id.imageViewSwitchModePrev);
        imageViewSwitchModeNext =           (ImageView) findViewById(R.id.imageViewSwitchModeNext);
        textviewSwitchMode      =           (TextView ) findViewById(R.id.textviewSwitchMode);

        statusActivityView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view,MotionEvent event) {
                checkRestartScreensaver();
                return true;

            }
        });

        //drive button toolbar
        driveButton =                       (Toolbar  ) findViewById(R.id.driveButton);

        setSupportActionBar(topToolbar);
        LayerDrawable settingsIcon = Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_settings);
        imageViewSettingsTransceiver.setImageDrawable(settingsIcon);
        imageViewSettingsBluetooth.setImageDrawable(settingsIcon);

        updateControlMode();

        imageViewExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickExit");
                exit();
            }
        });
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServices();
                startActivity(new Intent(StatusActivity.this, SettingsActivity.class));
                Log.d(TAG, "onClickSettings");
            }
        });

        imageViewSettingsBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServices();
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
                Log.d(TAG, "onClickSettingsBluetooth");
            }
        });

        imageViewSettingsTransceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServices();
                Log.d(TAG, "onClickSettingsTransceiver");
            }
        });

        imageViewSwitchModePrev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Utils.setIntPref(getString(R.string.pref_key_control_mode), getDataHandler().setControlModePrev());
                Intent updateControlModeIntent = new Intent(StatusActivity.this, WatchdogService.class);
                updateControlModeIntent.setAction(Constants.ACTION.CONTROL_MODE_CHANGED);
                startService(updateControlModeIntent);
                updateControlMode();
                Log.d(TAG, "onClickSwitchModePrev");
            }
        });

        imageViewSwitchModeNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Utils.setIntPref(getString(R.string.pref_key_control_mode), getDataHandler().setControlModeNext());
                Intent updateControlModeIntent = new Intent(StatusActivity.this, WatchdogService.class);
                updateControlModeIntent.setAction(Constants.ACTION.CONTROL_MODE_CHANGED);
                startService(updateControlModeIntent);
                updateControlMode();
                Log.d(TAG, "onClickSwitchModeNext");
            }
        });

        driveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatusActivity.this, DriveActivity.class));
                Log.d(TAG, "onClickDrive");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!onExit) {
            LocalBroadcastManager.getInstance(this).registerReceiver(serialStatusChangeReceiver, serialStatusChangeFilter);
            LocalBroadcastManager.getInstance(this).registerReceiver(ipStatusChangeReceiver, ipStatusChangeFilter);
            updateControlMode();
            startService(new Intent(StatusActivity.this, WatchdogService.class));
            checkRestartScreensaver();
        }
        Log.d(TAG, "onStatusActivityResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serialStatusChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ipStatusChangeReceiver);
        abortScreensaver();
        onExit = false;
        Log.d(TAG, "onStatusActivityPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServices();
        unregisterReceiver(usbReciever);
        Log.d(TAG, "onStatusActivityDestroy");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action == null) {
            Log.d(TAG,"Notification send empty intent");
            return;
        }
        switch (action) {
            case Constants.ACTION.EXIT:
                Log.d(TAG, "Notification send action EXIT");
                exit();
                break;
            default:
                Log.e(TAG, "Notification send unknown intent");
                break;
        }
    }

    private void exit(){
        onExit = true;
        stopServices();
        moveTaskToBack(true);
    }

    private void stopServices() {
        if(!WatchdogService.getIsDestroyed()){
            stopService(new Intent(StatusActivity.this, WatchdogService.class));
        }
    }

    private void updateControlMode(){
        imageViewSettingsTransceiver.setVisibility(View.INVISIBLE);
        imageViewSettingsBluetooth.setVisibility(View.INVISIBLE);

        switch (carduino.dataHandler.getControlMode()){
            case TRANSCEIVER:
                textviewSwitchMode.setText(R.string.controlModeTransceiver);
                imageViewDeviceRemoteIp.setImageResource(R.drawable.device_remote);
                textViewDeviceRemoteIpName.setText(R.string.labelIpRemote);
                imageViewDeviceTransceiver.setImageResource(R.drawable.device_mobile_transceive);
                textViewDeviceTransceiverIpName.setText(R.string.labelIpLocal);
                //visible if using bluetooth
                if(getDataHandler().getSerialPref().isAutoBluetooth() || getDataHandler().getSerialPref().isNone()){
                    imageViewSettingsBluetooth.setVisibility(View.VISIBLE);
                }
                if(getData().getSerialState().isUnknown()) {
                    getData().setSerialState(new ConnectionState(ConnectionEnum.IDLE, ""));
                }
                updateIp();
                break;
            case REMOTE:
                imageViewSettingsTransceiver.setVisibility(View.VISIBLE);
                textviewSwitchMode.setText(R.string.controlModeRC);
                imageViewDeviceRemoteIp.setImageResource(R.drawable.device_mobile_send);
                textViewDeviceRemoteIpName.setText(R.string.labelIpLocal);
                textViewDeviceTransceiverIpName.setText(R.string.labelIpTransceiver);
                imageViewDeviceTransceiver.setImageResource(R.drawable.device_mobile_na);
                getData().setSerialState(new ConnectionState(ConnectionEnum.UNKNOWN, ""));
                updateIp();
                break;
            default://DIRECT
                textviewSwitchMode.setText(R.string.controlModeDirect);
                //always visible
                imageViewSettingsBluetooth.setVisibility(View.VISIBLE);
                imageViewDeviceRemoteIp.setImageResource(R.drawable.device_mobile_send);
                textViewDeviceRemoteIpName.setText(R.string.labelIpDirect);
                imageViewDeviceTransceiver.setImageResource(R.drawable.device_no_ip_device);
                textViewDeviceTransceiverIpName.setText("");
                textViewDeviceTransceiverIp.setText("");
                textViewDeviceRemoteIp.setText("");
                if(getData().getSerialState().isUnknown()) {
                    getData().setSerialState(new ConnectionState(ConnectionEnum.IDLE, ""));
                }
                break;
        }
        updateStatus();
    }

    private void updateStatus(){
        textViewDeviceArduino.setText(getData().getSerialName());
        if(getData().getSerialType().isBluetooth()){
            textViewDeviceArduinoName.setText(R.string.serialDeviceBluetooth);
        }
        else{
            textViewDeviceArduinoName.setText(R.string.serialDeviceArduino);
        }
        //serial connection
        imageViewSerialConnection.setImageDrawable(getData().getSerialConnLogoId(getDataHandler().getSerialPref()));
        textViewSerialConnection.setText(R.string.serialConnection);
        textViewSerialConnectionStatus.setText(getData().getSerialState().getStateName());
        textViewSerialConnectionError.setText(getData().getSerialState().getError());
        if(!getData().getSerialState().getError().equals("")){
            //set focus for marquee
            textViewSerialConnectionError.setSelected(true);
        }

        switch (getDataHandler().getControlMode()){
            case DIRECT:
                //ip connection
                imageViewIpConnection.setImageDrawable(Utils.assembleDrawables(R.drawable.status_no_ip_device));
                textViewIpConnection.setText("");
                textViewIpConnectionStatus.setText("");
                textViewIpConnectionError.setText("");
                break;
            default:
                //ip connection
                try {
                    textViewIpConnection.setText(R.string.ipConnection);
                    imageViewIpConnection.setImageDrawable((getDData()).getIpConnLogoId());
                    textViewIpConnectionStatus.setText(getDData().getIpState().getStateName());
                    textViewIpConnectionError.setText(getDData().getIpState().getError());
                    if (!getDData().getIpState().getError().equals("")) {
                        //set focus for marquee
                        textViewIpConnectionError.setSelected(true);
                    }
                }catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                updateIp();

                break;
        }
    }

    private void updateIp(){
        //// TODO: 12.01.201 implement
        try{
            textViewDeviceRemoteIp.setText(getDData().getRemoteIp());
            textViewDeviceTransceiverIp.setText(getDData().getTransceiverIp());
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    private class SerialStatusActivityStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "serial status change event: " + getData().getSerialState().getStateName());
            updateStatus();
        }
    }

    private class IpStatusActivityStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "ip status change event: " + getDData().getIpState().getStateName());
            updateStatus();
        }
    }

    class UsbBroadcastReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.PERMISSION.USB)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            startService(new Intent(StatusActivity.this, SerialService.class));
                            Log.d(TAG, "restart serialService");
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.d(TAG, "ACTION_USB_DEVICE_DETACHED");
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        Log.d(TAG, "EXTRA_DEVICE");
                        // call your method that cleans up and closes communication with the device
                        Log.d(TAG, "disconnecting from usb device");

                        stopService(new Intent(StatusActivity.this, SerialService.class));
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                startService(new Intent(StatusActivity.this, SerialService.class));
                Log.d(TAG, "usb cable: serialService started");
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                stopService(new Intent(StatusActivity.this, SerialService.class));
                Log.d(TAG, "usb cable: serialService stopped");
            }
        }
    }

    private final Runnable triggerScreensaverRunnable = new Runnable() {
        @Override
        public void run() {
            abortScreensaver();
            startActivity(new Intent(StatusActivity.this,ScreensaverActivity.class));
        }
    };
    private void abortScreensaver() {
        screensaverHandler.removeCallbacks(triggerScreensaverRunnable);
        //Log.d(TAG, "screensaverCounter stopped");
    }
    private void triggerScreensaver(){
        if(getDataHandler().getScreensaver() >=0) {
            screensaverHandler.postDelayed(triggerScreensaverRunnable, getDataHandler().getScreensaver());
            //Log.d(TAG, "screensaverCounter started");
        }
    }
    private void restartScreensaver(){
        abortScreensaver();
        triggerScreensaver();
    }

    private void checkRestartScreensaver(){
        if(getDataHandler().getControlMode().isTransceiver()){
            restartScreensaver();
        }
        else{
            abortScreensaver();
        }
    }
}
