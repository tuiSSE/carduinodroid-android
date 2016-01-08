package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
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

    private ImageView imageViewExit;
    private ImageView imageViewSettings;

    private ImageView imageViewSettingsRemoteIp;
    private ImageView imageViewSettingsTransceiver;
    private ImageView imageViewSettingsBluetooth;
    private Toolbar driveButton;
/*

    private Button driveButton;
    private Button startSerialButton;
    private Button stopSerialButton;
    */
    private ImageView imageViewConnectionSerial;
    private ImageView imageViewDeviceArduino;
    private ImageView imageViewDeviceRemoteIp;
    private TextView textViewSerialConnectionStatus;
    private CarduinodroidApplication carduino;

    private IntentFilter serialConnectionStatusChangeFilter;
    private SerialConnectionStatusChangeReceiver serialConnectionStatusChangeReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.carduino = (CarduinodroidApplication) getApplication();
        setContentView(R.layout.activity_status);
        // prevent the application from switching to landscape-mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        serialConnectionStatusChangeReceiver = new SerialConnectionStatusChangeReceiver();
        serialConnectionStatusChangeFilter = new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));

        //get the Views

        Toolbar topToolbar = (Toolbar) findViewById(R.id.topToolbar);
        setSupportActionBar(topToolbar);
        driveButton = (Toolbar) findViewById(R.id.driveButton);
        imageViewExit = (ImageView) findViewById(R.id.imageViewExit);
        imageViewSettings = (ImageView) findViewById(R.id.imageViewSettings);

        imageViewDeviceArduino = (ImageView) findViewById(R.id.imageViewDeviceArduino);
        imageViewDeviceRemoteIp = (ImageView) findViewById(R.id.imageViewDeviceRemoteIp);
        /*
        closeButton = (Button) findViewById(R.id.buttonClose);
        settingsButton = (Button) findViewById(R.id.buttonSettings);
        driveButton = (Button) findViewById(R.id.buttonDrive);
        startSerialButton = (Button) findViewById(R.id.buttonSerialStart);
        stopSerialButton = (Button) findViewById(R.id.buttonSerialStop);
        */
        textViewSerialConnectionStatus = (TextView) findViewById(R.id.textViewSerialConnectionStatus);
        imageViewConnectionSerial = (ImageView) findViewById(R.id.imageViewConnectionSerial);

        imageViewSettingsRemoteIp = (ImageView) findViewById(R.id.imageViewSettingsRemoteIp);
        imageViewSettingsTransceiver = (ImageView) findViewById(R.id.imageViewSettingsTransceiver);
        imageViewSettingsBluetooth = (ImageView) findViewById(R.id.imageViewSettingsBluetooth);

        Drawable[] layers = new Drawable[2];
        layers[0] = getResources().getDrawable(R.drawable.buttonshape_primary_light);
        layers[1] = getResources().getDrawable(R.drawable.icon_settings);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        imageViewSettingsRemoteIp.setImageDrawable(layerDrawable);
        imageViewSettingsTransceiver.setImageDrawable(layerDrawable);
        imageViewSettingsBluetooth.setImageDrawable(layerDrawable);


        textViewSerialConnectionStatus.setText("");

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

        driveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatusActivity.this, DriveActivity.class));
                Log.d(TAG, "onClickDrive");
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);
        registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter);
        imageViewConnectionSerial.setImageResource(carduino.dataContainer.serialData.getSerialConnLogoId());
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
                return;
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    private class SerialConnectionStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive status change event");
            String status = intent.getStringExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_STATE));
            String name = intent.getStringExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_NAME));
            int resId = intent.getIntExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_LOGO), R.drawable.serial_type_none);
            textViewSerialConnectionStatus.setText(String.format(getString(R.string.serialConnectionStatus),status,name));
            imageViewConnectionSerial.setImageResource(resId);
        }
    }
}
