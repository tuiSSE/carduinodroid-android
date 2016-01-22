package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "CarduinoStatusActivity";

    //main toolbar
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

    private IntentFilter serialConnectionStatusChangeFilter;
    private SerialConnectionStatusActivityStatusChangeReceiver serialConnectionStatusChangeReceiver;
    private UsbBroadcastReciever usbReciever;

    private IntentFilter usbFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.carduino = (CarduinodroidApplication) getApplication();
        setContentView(R.layout.activity_status);
        // prevent the application from switching to landscape-mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        serialConnectionStatusChangeReceiver =  new SerialConnectionStatusActivityStatusChangeReceiver();
        serialConnectionStatusChangeFilter =    new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));

        usbReciever = new UsbBroadcastReciever();
        usbFilter = new IntentFilter(getString(R.string.USB_PERMISSION));
        registerReceiver(usbReciever, usbFilter);

        //get the Views
        Toolbar topToolbar =                (Toolbar  ) findViewById(R.id.topToolbar);
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
                stopService(new Intent(StatusActivity.this, SerialService.class));
                moveTaskToBack(true);
            }
        });
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatusActivity.this, SettingsActivity.class));
                Log.d(TAG, "onClickSettings");
            }
        });

        imageViewDeviceArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(StatusActivity.this, SerialService.class));
                Log.d(TAG, "onClickSerialStart");
            }
        });

        imageViewDeviceRemoteIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(StatusActivity.this, SerialService.class));
                Log.d(TAG, "onClickSerialStop");

            }
        });
        imageViewSettingsBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatusActivity.this,ChooseBluetoothDeviceActivity.class));
                Log.d(TAG, "onClickSettingsBluetooth");
            }
        });

        imageViewSwitchModePrev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Utils.setIntPref(getString(R.string.pref_key_control_mode),carduino.dataContainer.preferences.setControlModePrev());
                updateControlMode();
                Log.d(TAG, "onClickSwitchModePrev");
            }
        });

        imageViewSwitchModeNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Utils.setIntPref(getString(R.string.pref_key_control_mode), carduino.dataContainer.preferences.setControlModeNext());
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
        //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);
        registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter);
        updateControlMode();
        Log.d(TAG, "onStatusActivityResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serialConnectionStatusChangeReceiver);
        Log.d(TAG, "onStatusActivityPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStatusActivityStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onStatusActivityRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStatusActivityStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            case SerialService.EXIT_ACTION:
                Log.d(TAG,"Notification send EXIT_ACTION");
                stopService(new Intent(StatusActivity.this, SerialService.class));
                moveTaskToBack(true);
                break;
            default:
                Log.d(TAG,"Notification send unknown intent");
                break;
        }
    }

    private void updateControlMode(){
        //stop services
        //stopService(new Intent(StatusActivity.this, SerialService.class));

        imageViewSettingsTransceiver.setVisibility(View.INVISIBLE);
        imageViewSettingsBluetooth.setVisibility(View.INVISIBLE);

        switch (carduino.dataContainer.preferences.getControlMode()){
            case TRANSCEIVER:
                textviewSwitchMode.setText(R.string.controlModeTransceiver);
                imageViewDeviceRemoteIp.setImageResource(R.drawable.device_remote);
                textViewDeviceRemoteIpName.setText(R.string.labelIpRemote);
                imageViewDeviceTransceiver.setImageResource(R.drawable.device_mobile_transceive);
                textViewDeviceTransceiverIpName.setText(R.string.labelIpLocal);
                //visible if using bluetooth
                if(carduino.dataContainer.preferences.getSerialPref().isAutoBluetooth() || carduino.dataContainer.preferences.getSerialPref().isNone()){
                    imageViewSettingsBluetooth.setVisibility(View.VISIBLE);
                }
                if(carduino.dataContainer.serialData.getSerialState().isUnknown()) {
                    carduino.dataContainer.serialData.setSerialState(new ConnectionState(ConnectionEnum.IDLE, ""));
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
                carduino.dataContainer.serialData.setSerialState(new ConnectionState(ConnectionEnum.UNKNOWN, ""));
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
                if(carduino.dataContainer.serialData.getSerialState().isUnknown()) {
                    carduino.dataContainer.serialData.setSerialState(new ConnectionState(ConnectionEnum.IDLE, ""));
                }
                break;
        }
        updateStatus();

        //start services

    }

    private void updateStatus(){
        textViewDeviceArduino.setText(carduino.dataContainer.serialData.getSerialName());
        if(carduino.dataContainer.serialData.getSerialType().isBluetooth()){
            textViewDeviceArduinoName.setText(R.string.serialDeviceBluetooth);
        }
        else{
            textViewDeviceArduinoName.setText(R.string.serialDeviceArduino);
        }
        //serial connection
        imageViewSerialConnection.setImageDrawable(carduino.dataContainer.serialData.getSerialConnLogoId(carduino.dataContainer.preferences.getSerialPref()));
        textViewSerialConnection.setText(R.string.serialConnection);
        textViewSerialConnectionStatus.setText(carduino.dataContainer.serialData.getSerialState().getStateName());
        textViewSerialConnectionError.setText(carduino.dataContainer.serialData.getSerialState().getError());
        if(!carduino.dataContainer.serialData.getSerialState().getError().equals("")){
            //set focus for marquee
            textViewSerialConnectionError.setSelected(true);
        }

        switch (carduino.dataContainer.preferences.getControlMode()){
            case DIRECT:
                //ip connection
                imageViewIpConnection.setImageDrawable(Utils.assembleDrawables(R.drawable.status_no_ip_device));
                textViewIpConnection.setText("");
                textViewIpConnectionStatus.setText("");
                textViewIpConnectionError.setText("");
                break;
            default:
                //ip connection
                imageViewIpConnection.setImageDrawable(carduino.dataContainer.ipData.getSerialConnLogoId());
                textViewIpConnection.setText(R.string.ipConnection);
                textViewIpConnectionStatus.setText(carduino.dataContainer.ipData.getIpState().getStateName());
                textViewIpConnectionError.setText(carduino.dataContainer.ipData.getIpState().getError());
                if(!carduino.dataContainer.ipData.getIpState().getError().equals("")){
                    //set focus for marquee
                    textViewIpConnectionError.setSelected(true);
                }
                updateIp();
                break;
        }
    }

    private void updateIp(){
        //// TODO: 12.01.201 implement
        textViewDeviceRemoteIp.setText(carduino.dataContainer.ipData.getRemoteIp());
        textViewDeviceTransceiverIp.setText(carduino.dataContainer.ipData.getTransceiverIp());
    }

    private class SerialConnectionStatusActivityStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive status change event: " + carduino.dataContainer.serialData.getSerialState().getStateName());
            updateStatus();
        }
    }



    class UsbBroadcastReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (getString(R.string.USB_PERMISSION).equals(action)) {
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


}
