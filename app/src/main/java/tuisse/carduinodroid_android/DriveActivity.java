package tuisse.carduinodroid_android;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoDriveActivity";
    private final float MAX_SPEED_DEGREE = 60;
    private final float MAX_STEER_DEGREE = 60;

    private CarduinodroidApplication carduino;

    private SerialDataRxReceiver serialDataRxReceiver;
    private IntentFilter serialDataRxFilter;
    private SerialConnectionDriveActivityStatusChangeReceiver serialConnectionStatusChangeReceiver;
    private IntentFilter serialConnectionStatusChangeFilter;

    private SensorManager sensorManager;
    private Sensor magnetSensor;
    private Sensor accelerationSensor;

    private float mAngle0Azimuth =0;
    private float mAngle1Pitch =0;
    private float mAngle2Roll =0;

    private float mAngle0FilteredAzimuth =0;
    private float mAngle1FilteredPitch =0;
    private float mAngle2FilteredRoll =0;

    private float mAngle0OffsetAzimuth =0;
    private float mAngle1OffsetPitch =0;
    private float mAngle2OffsetRoll =0;
    private boolean sensorMeasureStart = true;


    //sensor calculation values
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private float rMat[] = new float[9];
    private float iMat[] = new float[9];
    private float orientation[] = new float[3];
    private int rotation;


    private  boolean statusLedState = false;
    private  boolean frontLightState = false;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View viewVideo;
    private View viewStop;
    private View viewDebug;

    private CheckBox checkBoxFailsafeStop;
    private CheckBox checkBoxOrientation;
    private CheckBox checkBoxDebug;

    private VerticalSeekBar seekBarSpeed;
    private SeekBar         seekBarSteer;

    private TextView textViewSpeed;
    private TextView textViewSteer;
    private TextView textViewAngle0;
    private TextView textViewAngle1;
    private TextView textViewAngle2;

    private TextView textViewDistanceFront;
    private TextView textViewDistanceBack;
    private TextView textViewAbsBattery;
    private TextView textViewRelBattery;
    private TextView textViewCurrent;
    private TextView textViewVoltage;
    private TextView textViewTemperature;

    private FloatingActionButton buttonDrive;
    private FloatingActionButton buttonHorn;
    private FloatingActionButton buttonFrontLight;
    private FloatingActionButton buttonStatusLed;


    private void reset(){
        carduino.dataContainer.serialData.serialTx.reset();
        seekBarSpeed.setMax(254);
        seekBarSteer.setMax(254);
        seekBarSpeed.setProgress(127);
        seekBarSteer.setProgress(127);
        seekBarSpeed.setSecondaryProgress(127);
        seekBarSteer.setSecondaryProgress(127);


        //checkBoxStatus.setChecked(false);
        //checkBoxFrontLight.setChecked(false);
        checkBoxFailsafeStop.setChecked(true);

        textViewSpeed.setText(String.format(getString(R.string.driveSpeed), 0));
        textViewSteer.setText(String.format(getString(R.string.driveSteer), 0));

        textViewDistanceFront.setText(String.format(getString(R.string.driveDistanceFront), getString(R.string.driveNotAvailable)));
        textViewDistanceBack.setText(String.format(getString(R.string.driveDistanceBack),  getString(R.string.driveNotAvailable)));
        textViewAbsBattery.setText(String.format(getString(R.string.driveAbsoluteBattery), getString(R.string.driveNotAvailable)));
        textViewRelBattery.setText(String.format(getString(R.string.driveRelativeBattery),  getString(R.string.driveNotAvailable)));
        textViewCurrent.setText(String.format(getString(R.string.driveCurrent),  getString(R.string.driveNotAvailable)));
        textViewVoltage.setText(String.format(getString(R.string.driveVoltage),  getString(R.string.driveNotAvailable)));
        textViewTemperature.setText(String.format(getString(R.string.driveTemperature),  getString(R.string.driveNotAvailable)));
    }

    private void refresh(){
        textViewDistanceFront.setText(String.format(getString(R.string.driveDistanceFront),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundFront())));
        textViewDistanceBack.setText(String.format(getString(R.string.driveDistanceBack),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getUltrasoundBack())));
        textViewAbsBattery.setText(String.format(getString(R.string.driveAbsoluteBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getAbsoluteBatteryCapacity())));
        textViewRelBattery.setText(String.format(getString(R.string.driveRelativeBattery),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getPercentBatteryCapacity())));
        textViewCurrent.setText(String.format(getString(R.string.driveCurrent),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getCurrent())));
        textViewVoltage.setText(String.format(getString(R.string.driveVoltage),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getVoltage())));
        textViewTemperature.setText(String.format(getString(R.string.driveTemperature),
                String.valueOf(carduino.dataContainer.serialData.serialRx.getDs2745Temperature())));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carduino = (CarduinodroidApplication) getApplication();
        setContentView(R.layout.activity_drive);

        serialConnectionStatusChangeReceiver = new SerialConnectionDriveActivityStatusChangeReceiver();
        serialConnectionStatusChangeFilter = new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
        //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);

        serialDataRxReceiver = new SerialDataRxReceiver();
        serialDataRxFilter = new IntentFilter(getString(R.string.SERIAL_DATA_RX_RECEIVED));

        checkBoxFailsafeStop    = (CheckBox) findViewById(R.id.checkBoxFailsafeStop);
        checkBoxOrientation     = (CheckBox) findViewById(R.id.checkBoxOrientation);
        checkBoxDebug           = (CheckBox) findViewById(R.id.checkBoxDebug);
        seekBarSpeed            = (VerticalSeekBar)  findViewById(R.id.seekBarSpeed);
        seekBarSteer            = (SeekBar)  findViewById(R.id.seekBarSteer);

        textViewSpeed           = (TextView) findViewById(R.id.textViewSpeed);
        textViewSteer           = (TextView) findViewById(R.id.textViewSteer);
        textViewAngle0         = (TextView) findViewById(R.id.textViewAngle0);
        textViewAngle1         = (TextView) findViewById(R.id.textViewAngle1);
        textViewAngle2         = (TextView) findViewById(R.id.textViewAngle2);

        textViewDistanceFront   = (TextView) findViewById(R.id.textViewDistanceFront);
        textViewDistanceBack    = (TextView) findViewById(R.id.textViewDistanceBack);
        textViewAbsBattery      = (TextView) findViewById(R.id.textViewAbsBattery);
        textViewRelBattery      = (TextView) findViewById(R.id.textViewRelBattery);
        textViewCurrent         = (TextView) findViewById(R.id.textViewCurrent);
        textViewVoltage         = (TextView) findViewById(R.id.textViewVoltage);
        textViewTemperature     = (TextView) findViewById(R.id.textViewTemperature);

        buttonDrive             = (FloatingActionButton) findViewById(R.id.buttonDrive);
        buttonHorn              = (FloatingActionButton) findViewById(R.id.buttonHorn);
        buttonFrontLight        = (FloatingActionButton) findViewById(R.id.buttonFrontLight);
        buttonStatusLed         = (FloatingActionButton) findViewById(R.id.buttonStatusLed);

        buttonDrive.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
        buttonHorn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
        buttonFrontLight.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
        buttonStatusLed.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));

        buttonDrive.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonHorn.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonFrontLight.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonStatusLed.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));

        viewDebug = findViewById(R.id.fullscreen_content_debug);
        viewVideo = findViewById(R.id.fullscreen_content_video);
        viewStop  = findViewById(R.id.fullscreen_content_stop);

        rotation = Utils.setScreenOrientation(DriveActivity.this,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        checkBoxOrientation.setText(String.format(getString(R.string.driveOrientation), getString(R.string.orientationLandscape)));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        buttonDrive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goDrive();
                        // register this class as a listener for the orientation and
                        // accelerometer sensors
                        sensorManager.registerListener(accelerometerListener,
                                accelerationSensor,
                                SensorManager.SENSOR_DELAY_GAME);
                        sensorManager.registerListener(magnetometerListener,
                                magnetSensor,
                                SensorManager.SENSOR_DELAY_GAME);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            buttonDrive.setImageDrawable(getDrawable(R.drawable.icon_drive_press));
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            buttonDrive.setImageDrawable(getDrawable(R.drawable.icon_drive));
                        }
                        sensorManager.unregisterListener(magnetometerListener);
                        sensorManager.unregisterListener(accelerometerListener);
                        sensorMeasureStart = true;
                        goStop();
                        reset();
                        return true;
                    default:
                        return false;
                }
            }
        });
        buttonStatusLed.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        statusLedState = !statusLedState;
                        if(statusLedState){
                            carduino.dataContainer.serialData.serialTx.setStatusLed(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonStatusLed.setImageDrawable(getDrawable(R.drawable.icon_status_led_press));
                            }
                        }
                        else{
                            carduino.dataContainer.serialData.serialTx.setStatusLed(0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonStatusLed.setImageDrawable(getDrawable(R.drawable.icon_status_led));
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        buttonFrontLight.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        frontLightState = !frontLightState;
                        if(frontLightState){
                            carduino.dataContainer.serialData.serialTx.setFrontLight(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonFrontLight.setImageDrawable(getDrawable(R.drawable.icon_front_light_press));
                            }

                        }
                        else{
                            carduino.dataContainer.serialData.serialTx.setFrontLight(0);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonFrontLight.setImageDrawable(getDrawable(R.drawable.icon_front_light));
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });


        checkBoxFailsafeStop.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if (checkBoxFailsafeStop.isChecked()) {
                    val = 1;
                }
                carduino.dataContainer.serialData.serialTx.setFailSafeStop(val);
            }
        });
        checkBoxOrientation.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v) {
                String rs = getString(R.string.orientationLandscape);
                if(checkBoxOrientation.isChecked()) {
                    rotation = Utils.setScreenOrientation(DriveActivity.this,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    rotation = Utils.setScreenOrientation(DriveActivity.this,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                if(rotation == Surface.ROTATION_0){
                    rs = getString(R.string.orientationPortrait);
                }
                checkBoxOrientation.setText(String.format(getString(R.string.driveOrientation), rs));

            }
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSpeed.setText(String.format(getString(R.string.driveSpeed), (progress - 127)));
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
                textViewSteer.setText(String.format(getString(R.string.driveSteer), (progress - 127)));
                carduino.dataContainer.serialData.serialTx.setSteer(progress - 127);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if(carduino.dataContainer.preferences.getControlMode().isTransceiver()){
            buttonDrive.setVisibility(View.INVISIBLE);
        }
        initUI();
        goStop();
        reset();
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


    private final Runnable initSystemUIRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
        // Delayed removal of status and navigation bar
        // Note that some of these constants are new as of API 16 (Jelly Bean)// and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        viewVideo.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE | //hide status bar
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | //API 19
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //API 32
        );
        }
    };
    private final Runnable showSystemUIRunable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            viewDebug.setVisibility(View.GONE);
            viewStop.setVisibility(View.VISIBLE);
            buttonDrive.setAlpha(1.0f);
            buttonHorn.setAlpha(1.0f);
            buttonFrontLight.setAlpha(1.0f);
            buttonStatusLed.setAlpha(1.0f);
        }
    };
    private final Runnable goStopRunnable = new Runnable() {
        @Override
        public void run() {
            viewDebug.setVisibility(View.GONE);
            viewStop.setVisibility(View.VISIBLE);
            buttonDrive.setAlpha(1.0f);
            buttonHorn.setAlpha(1.0f);
            buttonFrontLight.setAlpha(1.0f);
            buttonStatusLed.setAlpha(1.0f);
        }
    };
    private final Runnable goDriveRunnable = new Runnable() {
        @Override
        public void run() {
            viewStop.setVisibility(View.GONE);
            if(checkBoxDebug.isChecked()) {
                viewDebug.setVisibility(View.VISIBLE);
                if(!carduino.dataContainer.preferences.getControlMode().isDirect()) {
                    buttonDrive.setAlpha(0.6f);
                    buttonHorn.setAlpha(0.6f);
                    buttonFrontLight.setAlpha(0.6f);
                    buttonStatusLed.setAlpha(0.6f);
                }
            }
        }
    };

    private void initUI() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        goDrive();
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(goStopRunnable);
        mHideHandler.postDelayed(initSystemUIRunnable, UI_ANIMATION_DELAY);
    }

    private void goDrive() {
        mHideHandler.removeCallbacks(goStopRunnable);
        mHideHandler.postDelayed(goDriveRunnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void showAll() {
        // Show the system bar
        viewVideo.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mHideHandler.removeCallbacks(initSystemUIRunnable);
        mHideHandler.postDelayed(showSystemUIRunable,UI_ANIMATION_DELAY);
        goStop();
    }

    private void goStop() {
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(goDriveRunnable);
        mHideHandler.postDelayed(showSystemUIRunable, UI_ANIMATION_DELAY);
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
        textViewAngle0.setText(String.format(getString(R.string.driveAngle0),(int) mAngle0FilteredAzimuth));
        textViewAngle1.setText(String.format(getString(R.string.driveAngle1),(int) mAngle1FilteredPitch));
        textViewAngle2.setText(String.format(getString(R.string.driveAngle2),(int) mAngle2FilteredRoll));
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
        int speed;
        switch (rotation) {
            case Surface.ROTATION_0:
                speed = (int) (scale((-1.0f)*mAngle1FilteredPitch,MAX_SPEED_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_SPEED_MAX);
                break;
            case Surface.ROTATION_90:
                speed = (int) (scale(mAngle2FilteredRoll,MAX_SPEED_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_SPEED_MAX);
                break;
            case Surface.ROTATION_180:
                speed = (int) (scale(mAngle1FilteredPitch,MAX_SPEED_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_SPEED_MAX);
                break;
            default:
                speed = (int) (scale((-1.0f)*mAngle2FilteredRoll,MAX_SPEED_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_SPEED_MAX);
                break;
        }

        textViewSpeed.setText(String.format(getString(R.string.driveSpeed), speed));
        seekBarSpeed.setProgress(speed + carduino.dataContainer.serialData.serialTx.VAL_SPEED_MAX);
        carduino.dataContainer.serialData.serialTx.setSpeed(speed);
    }

    private void setSteering(){
        int steering;
        switch (rotation) {
            case Surface.ROTATION_0:
                steering = (int) (scale(mAngle2FilteredRoll,MAX_STEER_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_STEER_MAX);
                break;
            case Surface.ROTATION_90:
                steering = (int) (scale(mAngle1FilteredPitch,MAX_STEER_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_STEER_MAX);
                break;
            case Surface.ROTATION_180:
                steering = (int) (scale((-1.0f)*mAngle2FilteredRoll,MAX_STEER_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_STEER_MAX);
                break;
            default:
                steering = (int) (scale((-1.0f)*mAngle1FilteredPitch,MAX_STEER_DEGREE)*carduino.dataContainer.serialData.serialTx.VAL_STEER_MAX);
                break;
        }
        textViewSteer.setText(String.format(getString(R.string.driveSteer), steering));
        seekBarSteer.setProgress(steering+carduino.dataContainer.serialData.serialTx.VAL_STEER_MAX);
        carduino.dataContainer.serialData.serialTx.setSteer(steering);
    }
}
