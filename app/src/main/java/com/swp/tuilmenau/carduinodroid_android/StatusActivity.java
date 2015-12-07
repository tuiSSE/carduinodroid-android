package com.swp.tuilmenau.carduinodroid_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.swp.tuilmenau.carduinodroid2.R;

public class StatusActivity extends Activity {

    private static final String TAG = "CarduinoMainActivity";
    Button closeButton;
    Button settingsButton;
    Button startSerialButton;
    Button stopSerialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

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
                Log.d(TAG, "onClickSettings");
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

}
