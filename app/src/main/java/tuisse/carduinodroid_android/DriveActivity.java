package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoDriveActivity";
    private CarduinodroidApplication carduino;

    private SerialDataRxReceiver serialDataRxReceiver;
    private IntentFilter serialDataRxFilter;
    private SerialConnectionStatusChangeReceiver serialConnectionStatusChangeReceiver;
    private IntentFilter serialConnectionStatusChangeFilter;

    private Button buttonReset;
    private CheckBox checkBoxStatus;
    private CheckBox checkBoxFrontLight;
    private CheckBox checkBoxFailsafeStop;
    private CheckBox checkBoxResetAccCurrent;

    private SeekBar seekBarSpeed;
    private SeekBar seekBarSteer;

    private TextView textViewSpeed;
    private TextView textViewSteer;

    private TextView textViewDistanceFront;
    private TextView textViewDistanceBack;
    private TextView textViewAbsBattery;
    private TextView textViewRelBattery;
    private TextView textViewCurrent;
    private TextView textViewVoltage;
    private TextView textViewTemperature;

    private void reset(){
        carduino.dataContainer.serialData.serialTx.reset();
        seekBarSpeed.setMax(254);
        seekBarSteer.setMax(254);
        seekBarSpeed.setProgress(127);
        seekBarSteer.setProgress(127);

        checkBoxStatus.setChecked(false);
        checkBoxFrontLight.setChecked(false);
        checkBoxFailsafeStop.setChecked(true);
        checkBoxResetAccCurrent.setChecked(false);

        textViewSpeed.setText(String.format(getString(R.string.speed), 0));
        textViewSteer.setText(String.format(getString(R.string.steer), 0));

        textViewDistanceFront.setText(String.format(getString(R.string.distanceFront), getString(R.string.notAvailable)));
        textViewDistanceBack.setText(String.format(getString(R.string.distanceBack),  getString(R.string.notAvailable)));
        textViewAbsBattery.setText(String.format(getString(R.string.absoluteBattery), getString(R.string.notAvailable)));
        textViewRelBattery.setText(String.format(getString(R.string.relativeBattery),  getString(R.string.notAvailable)));
        textViewCurrent.setText(String.format(getString(R.string.current),  getString(R.string.notAvailable)));
        textViewVoltage.setText(String.format(getString(R.string.voltage),  getString(R.string.notAvailable)));
        textViewTemperature.setText(String.format(getString(R.string.temperature),  getString(R.string.notAvailable)));
    }

    private void refresh(){
        textViewDistanceFront.setText(String.format(getString(R.string.distanceFront),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundFront() )));
        textViewDistanceBack.setText(String.format(getString(R.string.distanceBack),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundBack() )));
        textViewAbsBattery.setText(String.format(getString(R.string.absoluteBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getAbsoluteBatteryCapacity() )));
        textViewRelBattery.setText(String.format(getString(R.string.relativeBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getPercentBatteryCapacity() )));
        textViewCurrent.setText(String.format(getString(R.string.current),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getCurrent() )));
        textViewVoltage.setText(String.format(getString(R.string.voltage),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getVoltage() )));
        textViewTemperature.setText(String.format(getString(R.string.temperature),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getDs2745Temperature() )));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        serialConnectionStatusChangeReceiver = new SerialConnectionStatusChangeReceiver();
        serialConnectionStatusChangeFilter = new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
        //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);

        serialDataRxReceiver = new SerialDataRxReceiver();
        serialDataRxFilter = new IntentFilter(getString(R.string.SERIAL_DATA_RX_RECEIVED));

        carduino = (CarduinodroidApplication) getApplication();
        buttonReset             = (Button) findViewById(R.id.buttonReset);
        checkBoxFrontLight      = (CheckBox) findViewById(R.id.checkBoxFrontLight);
        checkBoxStatus          = (CheckBox) findViewById(R.id.checkBoxStatus);
        checkBoxFailsafeStop    = (CheckBox) findViewById(R.id.checkBoxFailsafeStop);
        checkBoxResetAccCurrent = (CheckBox) findViewById(R.id.checkBoxResetAccCurrent);
        seekBarSpeed            = (SeekBar)  findViewById(R.id.seekBarSpeed);
        seekBarSteer            = (SeekBar)  findViewById(R.id.seekBarSteer);

        textViewSpeed           = (TextView) findViewById(R.id.textViewSpeed);
        textViewSteer           = (TextView) findViewById(R.id.textViewSteer);
        textViewDistanceFront   = (TextView) findViewById(R.id.textViewDistanceFront);
        textViewDistanceBack    = (TextView) findViewById(R.id.textViewDistanceBack);
        textViewAbsBattery      = (TextView) findViewById(R.id.textViewAbsBattery);
        textViewRelBattery      = (TextView) findViewById(R.id.textViewRelBattery);
        textViewCurrent         = (TextView) findViewById(R.id.textViewCurrent);
        textViewVoltage         = (TextView) findViewById(R.id.textViewVoltage);
        textViewTemperature     = (TextView) findViewById(R.id.textViewTemperature);

        reset();

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClickReset");
                reset();
            }
        });

        checkBoxStatus.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if (checkBoxStatus.isChecked()) {
                    val = 1;
                }
                carduino.dataContainer.serialData.serialTx.setStatusLed(val);
            }
        });

        checkBoxFrontLight.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxFrontLight.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialData.serialTx.setFrontLight(val);
            }
        });

        checkBoxResetAccCurrent.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxResetAccCurrent.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialData.serialTx.setResetAccCur(val);
            }
        });

        checkBoxFailsafeStop.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxFailsafeStop.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialData.serialTx.setFailSafeStop(val);
            }
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSpeed.setText(String.format(getString(R.string.speed), (progress - 127)));
                carduino.dataContainer.serialData.serialTx.setSpeed(progress - 127);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarSteer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSteer.setText(String.format(getString(R.string.steer), (progress - 127)));
                carduino.dataContainer.serialData.serialTx.setSteer(progress - 127);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter);
        registerReceiver(serialDataRxReceiver, serialDataRxFilter);
        refresh();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(serialConnectionStatusChangeReceiver);
        unregisterReceiver(serialDataRxReceiver);
    }

    private class SerialDataRxReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG,"onDriveActivityReceiverReceive");
            refresh();
        }
    }

    private class SerialConnectionStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.toString().equals(R.string.SERIAL_CONNECTION_STATUS_CHANGED)){
                Log.d(TAG,"onReceive status change event");
                //TODO: update status
            }
        }
    }

}