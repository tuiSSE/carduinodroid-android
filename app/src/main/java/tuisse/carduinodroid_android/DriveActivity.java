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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

    private SensorManager sensorManager;
    private Sensor magnetSensor;
    private Sensor accelerationSensor;

    float mAngle0Azimuth =0;
    float mAngle1Pitch =0;
    float mAngle2Roll =0;

    float mAngle0FilteredAzimuth =0;
    float mAngle1FilteredPitch =0;
    float mAngle2FilteredRoll =0;

    float mAngle0OffsetAzimuth =0;
    float mAngle1OffsetPitch =0;
    float mAngle2OffsetRoll =0;
    boolean sensorMeasureStart = true;

    //sensor calculation values
    float[] mGravity = null;
    float[] mGeomagnetic = null;
    float rMat[] = new float[9];
    float iMat[] = new float[9];
    float orientation[] = new float[3];

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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hide();
                        // register this class as a listener for the orientation and
                        // accelerometer sensors
                        sensorManager.registerListener(accelerometerListener,
                                accelerationSensor,
                                SensorManager.SENSOR_DELAY_GAME);
                        sensorManager.registerListener(magnetometerListener,
                                magnetSensor,
                                SensorManager.SENSOR_DELAY_GAME);
                        return true;
                    case MotionEvent.ACTION_UP:
                        sensorManager.unregisterListener(magnetometerListener);
                        sensorManager.unregisterListener(accelerometerListener);
                        sensorMeasureStart = true;
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
            //Log.d(TAG,"onDriveActivityReceiverReceive");
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

    private final Runnable mHideSystemUI = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
        // Delayed removal of status and navigation bar
        // Note that some of these constants are new as of API 16 (Jelly Bean)// and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            //View.SYSTEM_UI_FLAG_LOW_PROFILE | //hide status bar
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | //API 19
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | //API 32
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
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
        mHideHandler.postDelayed(mHideSystemUI, UI_ANIMATION_DELAY);
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
        mHideHandler.removeCallbacks(mHideSystemUI);
        show();
    }

    private void show() {
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        //mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private float restrictAngle(float tmpAngle){
        while(tmpAngle>=180) tmpAngle-=360;
        while(tmpAngle<-180) tmpAngle+=360;
        return tmpAngle;
    }

    //x is a raw angle value from getOrientation(...)
    //y is the current filtered angle value
    private float calculateFilteredAngle(float x, float y){
        final float alpha = 0.3f;
        float diff = x-y;

        //here, we ensure that abs(diff)<=180
        diff = restrictAngle(diff);

        y += alpha*diff;
        //ensure that y stays within [-180, 180[ bounds
        y = restrictAngle(y);

        return y;
    }

    public void processSensorData(){
        if (mGravity != null && mGeomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(rMat, iMat, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(rMat, orientation);
                mAngle0Azimuth = (float)Math.toDegrees((double)orientation[0]); // orientation contains: azimut, pitch and roll
                mAngle1Pitch = (float)Math.toDegrees((double)orientation[1]); //pitch
                mAngle2Roll = -(float)Math.toDegrees((double)orientation[2]); //roll

                if(sensorMeasureStart){
                    mAngle0OffsetAzimuth = mAngle0Azimuth;
                    mAngle1OffsetPitch = mAngle1Pitch;
                    mAngle2OffsetRoll = mAngle2Roll;
                    sensorMeasureStart = false;
                }

                mAngle0Azimuth = restrictAngle(mAngle0OffsetAzimuth - mAngle0Azimuth);
                mAngle1Pitch = restrictAngle(mAngle1OffsetPitch - mAngle1Pitch);
                mAngle2Roll = restrictAngle(mAngle2OffsetRoll - mAngle2Roll);

                mAngle0FilteredAzimuth = calculateFilteredAngle(mAngle0Azimuth, mAngle0FilteredAzimuth);
                mAngle1FilteredPitch = calculateFilteredAngle(mAngle1Pitch, mAngle1FilteredPitch);
                mAngle2FilteredRoll = calculateFilteredAngle(mAngle2Roll, mAngle2FilteredRoll);

                updateSensorData();
            }
            mGravity=null; //oblige full new refresh
            mGeomagnetic=null; //oblige full new refresh
        }
    }

    SensorEventListener accelerometerListener = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity = event.values.clone();
                processSensorData();
            }
        }
    };
    SensorEventListener magnetometerListener = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic = event.values.clone();
                processSensorData();
            }
        }
    };

    private void updateSensorData(){
        textViewCurrent.setText("azimut\t: " + String.valueOf((int) mAngle0FilteredAzimuth));
        textViewVoltage.setText("pitch\t: " + String.valueOf((int) mAngle1FilteredPitch));
        textViewTemperature.setText("roll\t: " + String.valueOf((int) mAngle2FilteredRoll));
        setSpeed();
        setSteering();
    }

    private float scale(float val, float bound){
        float calc = val/bound;
        if(calc > 1.0f){
            calc = 1.0f;
        }
        if (calc < -1.0f){
            calc = -1.0f;
        }
        return calc;
    }

    private void setSpeed(){
        int speed = (int) (scale((-1.0f)*mAngle1FilteredPitch,60)*127);
        textViewSpeed.setText(String.format(getString(R.string.speed), speed));
        carduino.dataContainer.serialData.serialTx.setSpeed(speed);
    }

    private void setSteering(){
        int steering = (int) (scale(mAngle2FilteredRoll,60)*127);
        textViewSteer.setText(String.format(getString(R.string.steer), steering));
        carduino.dataContainer.serialData.serialTx.setSteer(steering);
    }
}
