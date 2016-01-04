package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusActivity extends AppCompatActivity {

    private static final String TAG = "CarduinoStatusActivity";

    private Button closeButton;
    private Button settingsButton;
    private Button driveButton;
    private Button startSerialButton;
    private Button stopSerialButton;
    private ImageView imageViewSerialConnectionStatus;
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
        closeButton = (Button) findViewById(R.id.buttonClose);
        settingsButton = (Button) findViewById(R.id.buttonSettings);
        driveButton = (Button) findViewById(R.id.buttonDrive);
        startSerialButton = (Button) findViewById(R.id.buttonSerialStart);
        stopSerialButton = (Button) findViewById(R.id.buttonSerialStop);
        textViewSerialConnectionStatus = (TextView) findViewById(R.id.textViewSerialConnectionStatus);
        imageViewSerialConnectionStatus = (ImageView) findViewById(R.id.imageViewSerialConnectionStatus);
        textViewSerialConnectionStatus.setText("");

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickClose");
                stopService(new Intent(StatusActivity.this, SerialService.class));
                moveTaskToBack(true);

            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
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

        startSerialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(StatusActivity.this, SerialService.class));
                Log.d(TAG, "onClickSerialStart");
            }
        });

        stopSerialButton.setOnClickListener(new View.OnClickListener() {
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
        imageViewSerialConnectionStatus.setImageResource(carduino.dataContainer.serialData.getLogoId());
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
        Log.d(TAG,"Notification send intent");
        if (action == null) {
            return;
        }
        switch (action) {
            case SerialService.EXIT_ACTION:
                Log.d(TAG,"Notification send EXIT_ACTION");
                stopService(new Intent(StatusActivity.this, SerialService.class));
                break;
        }
    }

    private class SerialConnectionStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive status change event");
            String status = intent.getStringExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_STATE));
            String name = intent.getStringExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_NAME));
            int resId = intent.getIntExtra(getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_LOGO), R.drawable.serial_idle);
            textViewSerialConnectionStatus.setText(String.format(getString(R.string.serialConnectionStatus),status,name));
            imageViewSerialConnectionStatus.setImageResource(resId);
        }
    }
}
