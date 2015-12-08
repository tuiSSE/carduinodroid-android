package com.swp.tuilmenau.carduinodroid_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class StatusActivity extends Activity {

    private static final String TAG = "CarduinoMainActivity";
    private CarduinodroidApplication carduino;
    Button closeButton;
    Button settingsButton;
    Button startSerialButton;
    Button stopSerialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.carduino = (CarduinodroidApplication) getApplication();
        setContentView(R.layout.activity_status);
        // prevent the application from switching to landscape-mode
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        //get the Views
        closeButton = (Button) findViewById(R.id.buttonClose);
        settingsButton  = (Button) findViewById(R.id.buttonSettings);
        startSerialButton  = (Button) findViewById(R.id.buttonSerialStart);
        stopSerialButton  = (Button) findViewById(R.id.buttonSerialStop);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClickClose");
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

        startSerialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!carduino.isSerialServiceRunning()) {
                    startService(new Intent(StatusActivity.this, SerialService.class));
                    Log.d(TAG, "onClickSerialStart");
                }
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
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onStatusActivityResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onStatusActivityPause");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStatusActivityStop");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d(TAG, "onStatusActivityRestart");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStatusActivityStart");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(StatusActivity.this, SerialService.class));
        Log.d(TAG, "onStatusActivityDestroy");
    }

}
