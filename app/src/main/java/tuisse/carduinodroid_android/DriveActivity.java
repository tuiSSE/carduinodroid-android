package tuisse.carduinodroid_android;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
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
    private SerialConnectionDriveActivityStatusChangeReceiver serialConnectionStatusChangeReceiver;
    private IntentFilter serialConnectionStatusChangeFilter;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mValuesMagnet      = new float[3];
    private float[] mValuesAccel       = new float[3];
    private float[] mValuesOrientation = new float[3];

    private float[] mRotationMatrix    = new float[9];
    private float[] nRotationMatrix    = new float[9];

    private CheckBox checkBoxStatus;
    private CheckBox checkBoxFrontLight;
    private CheckBox checkBoxFailsafeStop;

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
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundFront())));
        textViewDistanceBack.setText(String.format(getString(R.string.distanceBack),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundBack())));
        textViewAbsBattery.setText(String.format(getString(R.string.absoluteBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getAbsoluteBatteryCapacity())));
        textViewRelBattery.setText(String.format(getString(R.string.relativeBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getPercentBatteryCapacity())));
        textViewCurrent.setText(String.format(getString(R.string.current),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getCurrent())));
        textViewVoltage.setText(String.format(getString(R.string.voltage),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getVoltage())));
        textViewTemperature.setText(String.format(getString(R.string.temperature),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getDs2745Temperature())));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        serialConnectionStatusChangeReceiver = new SerialConnectionDriveActivityStatusChangeReceiver();
        serialConnectionStatusChangeFilter = new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
        //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);

        serialDataRxReceiver = new SerialDataRxReceiver();
        serialDataRxFilter = new IntentFilter(getString(R.string.SERIAL_DATA_RX_RECEIVED));

        carduino = (CarduinodroidApplication) getApplication();
        checkBoxFrontLight      = (CheckBox) findViewById(R.id.checkBoxFrontLight);
        checkBoxStatus          = (CheckBox) findViewById(R.id.checkBoxStatus);
        checkBoxFailsafeStop    = (CheckBox) findViewById(R.id.checkBoxFailsafeStop);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hide();
                        // register this class as a listener for the orientation and
                        // accelerometer sensors
                        mSensorManager.registerListener(mEventListener,
                                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_GAME);
                        mSensorManager.registerListener(mEventListener,
                                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                SensorManager.SENSOR_DELAY_GAME);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSensorManager.unregisterListener(mEventListener);
                        show();
                        reset();
                        return true;
                }
                return false;
            }
        });
        hideAll();
        show();
        reset();

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
            Log.d(TAG,"onDriveActivityReceiverReceive");
            refresh();
        }
    }

    private class SerialConnectionDriveActivityStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive status change event");
            //TODO: update status
        }
    }

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LOW_PROFILE //hide status bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY //API 19
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //API 32
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };
    private View mControlsView;
    private final Runnable mShowAllPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private void hideAll() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        hide();
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void hide() {
        mControlsView.setVisibility(View.GONE);
    }

    @SuppressLint("InlinedApi")
    private void showAll() {
        // Show the system bar
        mContentView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        show();
    }

    private void show() {
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        //mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final SensorEventListener mEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                        break;
                }
                SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
                SensorManager.
                SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
                textViewTemperature.setText("rx: " + String.valueOf(mValuesOrientation[0]) + " ry: " + String.valueOf(mValuesOrientation[1]) + " rz: " + String.valueOf(mValuesOrientation[2]));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
