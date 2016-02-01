package tuisse.carduinodroid_android;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
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
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoDriveActivity";
    private final float MAX_SPEED_DEGREE = 20;
    private final float MAX_STEER_DEGREE = 20;

    private CarduinodroidApplication carduino;

    private SerialDataRxReceiver serialDataRxReceiver;
    private IntentFilter serialDataRxFilter;
    private SerialConnectionDriveActivityStatusChangeReceiver serialConnectionStatusChangeReceiver;
    private IntentFilter serialConnectionStatusChangeFilter;

    private SensorManager sensorManager;
    private Sensor magnetSensor;
    private Sensor accelerationSensor;

    private Sound sound;

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
    private View viewDistance;
    private View viewDebug;

    private CheckBox checkBoxFailsafeStop;
    private CheckBox checkBoxOrientation;
    private CheckBox checkBoxDebug;

    private VerticalSeekBar seekBarSpeed;
    private SeekBar         seekBarSteer;

    private ProgressBar     progressbarDistanceFront;
    private ProgressBar     progressbarDistanceBack;

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


    private void setDistance(ProgressBar pb, int distance){
        if((distance < 254) && (distance >= 0)){
            pb.setProgress(254-distance);
            pb.setAlpha(1.0f);
        }
        else{
            pb.setProgress(254);
            pb.setAlpha(0.6f);
        }
    }

    private void reset(){
        try {
            carduino.dataContainer.resetMotors();
            setDistance(progressbarDistanceFront, -1);
            setDistance(progressbarDistanceBack, -1);

            textViewSpeed.setText(String.format(getString(R.string.driveSpeed), carduino.dataContainer.getSpeed()));
            textViewSteer.setText(String.format(getString(R.string.driveSteer), carduino.dataContainer.getSteer()));
            textViewDistanceFront.setText(String.format(getString(R.string.driveDistanceFront), -1.0f));
            textViewDistanceBack.setText(String.format(getString(R.string.driveDistanceBack), -1.0f));
            textViewAbsBattery.setText(String.format(getString(R.string.driveAbsoluteBattery), -1.0f));
            textViewRelBattery.setText(String.format(getString(R.string.driveRelativeBattery), -1.0f));
            textViewCurrent.setText(String.format(getString(R.string.driveCurrent), -1.0f));
            textViewVoltage.setText(String.format(getString(R.string.driveVoltage), -1.0f));
            textViewTemperature.setText(String.format(getString(R.string.driveTemperature), -1.0f));
        }catch (Exception e){
            Log.e(TAG,"reset()"+e.toString());
        }
    }

    private void refresh(){
        try {
            textViewDistanceFront.setText(String.format(getString(R.string.driveDistanceFront),
                    carduino.dataContainer.getUltrasoundFront()));
            textViewDistanceBack.setText(String.format(getString(R.string.driveDistanceBack),
                    carduino.dataContainer.getUltrasoundBack()));
            setDistance(progressbarDistanceFront, Math.round(carduino.dataContainer.getUltrasoundFront()));
            setDistance(progressbarDistanceBack, Math.round(carduino.dataContainer.getUltrasoundBack()));
            textViewAbsBattery.setText(String.format(getString(R.string.driveAbsoluteBattery),
                    carduino.dataContainer.getAbsoluteBatteryCapacity()));
            textViewRelBattery.setText(String.format(getString(R.string.driveRelativeBattery),
                    carduino.dataContainer.getPercentBatteryCapacity()));
            textViewCurrent.setText(String.format(getString(R.string.driveCurrent),
                    carduino.dataContainer.getCurrent()));
            textViewVoltage.setText(String.format(getString(R.string.driveVoltage),
                    carduino.dataContainer.getVoltage()));
            textViewTemperature.setText(String.format(getString(R.string.driveTemperature),
                    carduino.dataContainer.getDs2745Temperature()));
        }catch (Exception e){
            Log.e(TAG,"refresh()"+e.toString());
        }
    }

    private void initView(){
        checkBoxFailsafeStop = (CheckBox) findViewById(R.id.checkBoxFailsafeStop);
        checkBoxOrientation = (CheckBox) findViewById(R.id.checkBoxOrientation);
        checkBoxDebug = (CheckBox) findViewById(R.id.checkBoxDebug);
        seekBarSpeed = (VerticalSeekBar) findViewById(R.id.seekBarSpeed);
        seekBarSteer = (SeekBar) findViewById(R.id.seekBarSteer);
        progressbarDistanceFront    = (ProgressBar) (findViewById(R.id.progressbarDistanceFront));
        progressbarDistanceBack     = (ProgressBar) (findViewById(R.id.progressbarDistanceBack));

        textViewSpeed = (TextView) findViewById(R.id.textViewSpeed);
        textViewSteer = (TextView) findViewById(R.id.textViewSteer);
        textViewAngle0 = (TextView) findViewById(R.id.textViewAngle0);
        textViewAngle1 = (TextView) findViewById(R.id.textViewAngle1);
        textViewAngle2 = (TextView) findViewById(R.id.textViewAngle2);

        textViewDistanceFront = (TextView) findViewById(R.id.textViewDistanceFront);
        textViewDistanceBack = (TextView) findViewById(R.id.textViewDistanceBack);
        textViewAbsBattery = (TextView) findViewById(R.id.textViewAbsBattery);
        textViewRelBattery = (TextView) findViewById(R.id.textViewRelBattery);
        textViewCurrent = (TextView) findViewById(R.id.textViewCurrent);
        textViewVoltage = (TextView) findViewById(R.id.textViewVoltage);
        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);

        buttonDrive = (FloatingActionButton) findViewById(R.id.buttonDrive);
        buttonHorn = (FloatingActionButton) findViewById(R.id.buttonHorn);
        buttonFrontLight = (FloatingActionButton) findViewById(R.id.buttonFrontLight);
        buttonStatusLed = (FloatingActionButton) findViewById(R.id.buttonStatusLed);

        if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            buttonDrive.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
            buttonHorn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
            buttonFrontLight.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
            buttonStatusLed.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryLight)));
        } else {
            //if Transceiver mode
            buttonDrive.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGreyed)));
            buttonHorn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGreyed)));
            buttonFrontLight.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGreyed)));
            buttonStatusLed.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGreyed)));
        }

        buttonDrive.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonHorn.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonFrontLight.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        buttonStatusLed.setRippleColor(ContextCompat.getColor(this, R.color.colorPrimary));


        viewDebug = findViewById(R.id.fullscreen_content_debug);
        viewVideo = findViewById(R.id.fullscreen_content_video);
        viewDistance = findViewById(R.id.fullscreen_content_distance);
        viewStop = findViewById(R.id.fullscreen_content_stop);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewDebug.getLayoutParams();
        if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            textViewAngle0.setVisibility(View.VISIBLE);
            textViewAngle1.setVisibility(View.VISIBLE);
            textViewAngle2.setVisibility(View.VISIBLE);
        } else {
            //if Transceiver mode
            params.addRule(RelativeLayout.ABOVE, R.id.fullscreen_content_stop);
            textViewAngle0.setVisibility(View.GONE);
            textViewAngle1.setVisibility(View.GONE);
            textViewAngle2.setVisibility(View.GONE);
        }
        viewDebug.setLayoutParams(params);

        buttonDrive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
                    //if Remote or Direct mode
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
                return false;
            }
        });

        buttonStatusLed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        statusLedState = !statusLedState;
                        if (statusLedState) {
                            carduino.dataContainer.setStatusLed(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonStatusLed.setImageDrawable(getDrawable(R.drawable.icon_status_led_press));
                            }
                        } else {
                            carduino.dataContainer.setStatusLed(0);
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

        buttonFrontLight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        frontLightState = !frontLightState;
                        if (frontLightState) {
                            carduino.dataContainer.setFrontLight(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonFrontLight.setImageDrawable(getDrawable(R.drawable.icon_front_light_press));
                            }

                        } else {
                            carduino.dataContainer.setFrontLight(0);
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

        buttonHorn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
                    //if Remote or Direct mode
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonHorn.setImageDrawable(getDrawable(R.drawable.icon_horn_press));
                            }
                            switch (carduino.dataContainer.preferences.getControlMode()) {
                                case DIRECT:
                                    sound.horn();
                                    break;
                                case REMOTE:
                                    //// TODO: 26.01.2016 implement
                                    break;
                                default:
                                    sound.horn();
                                    break;
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                buttonHorn.setImageDrawable(getDrawable(R.drawable.icon_horn));
                            }
                            switch (carduino.dataContainer.preferences.getControlMode()) {
                                case DIRECT:
                                    sound.stop();
                                    break;
                                case REMOTE:
                                    //// TODO: 26.01.2016 implement
                                    break;
                                default:
                                    sound.stop();
                                    break;
                            }
                            return true;
                        default:
                            return false;
                    }
                }
                return false;
            }
        });

        checkBoxDebug.setChecked(carduino.dataContainer.preferences.getDebugView());
        checkBoxDebug.setOnClickListener(new CheckBox.OnClickListener() {

            @Override
            public void onClick(View v) {
                int val = carduino.dataContainer.preferences.toggleDebugView();
                Utils.setIntPref(getString(R.string.pref_key_debug_view), val);
            }
        });
        checkBoxFailsafeStop.setChecked(carduino.dataContainer.preferences.getFailSafeStopPref());
        if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            checkBoxFailsafeStop.setEnabled(true);
            checkBoxFailsafeStop.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int val = carduino.dataContainer.preferences.toggleFailSafeStopPref();
                    Utils.setIntPref(getString(R.string.pref_key_failsafe_stop), val);
                    carduino.dataContainer.setFailSafeStop(val);
                }
            });
        } else {
            checkBoxFailsafeStop.setEnabled(false);
        }
        checkBoxOrientation.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rs = getString(R.string.orientationLandscape);
                if (checkBoxOrientation.isChecked()) {
                    driveLandscape();
                } else {
                    drivePortrait();
                }
                if (rotation == Surface.ROTATION_0) {
                    rs = getString(R.string.orientationPortrait);
                }
                checkBoxOrientation.setText(String.format(getString(R.string.driveOrientation), rs));
            }
        });
/*
        seekBarSpeed.setEnabled(false);
        seekBarSteer.setEnabled(false);

        if (!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textViewSpeed.setText(String.format(getString(R.string.driveSpeed), (progress - 127)));
                    carduino.dataContainer.setSpeed(progress - 127);
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
                    carduino.dataContainer.setSteer(progress - 127);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
*/


        seekBarSpeed.setMax(254);
        seekBarSteer.setMax(254);
        seekBarSpeed.setProgress(127);
        seekBarSteer.setProgress(127);
        seekBarSpeed.setSecondaryProgress(127);
        seekBarSteer.setSecondaryProgress(127);
    }

    private void driveLandscape(){
        setContentView(R.layout.activity_drive_landscape);
        initView();
        rotation = Utils.setScreenOrientation(DriveActivity.this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        checkBoxOrientation.setText(String.format(getString(R.string.driveOrientation), getString(R.string.orientationLandscape)));

        initUI();
        goStop();
        reset();
    }

    private void drivePortrait(){
        setContentView(R.layout.activity_drive_portrait);
        initView();
        rotation = Utils.setScreenOrientation(DriveActivity.this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        checkBoxOrientation.setText(String.format(getString(R.string.driveOrientation), getString(R.string.orientationPortrait)));

        initUI();
        goStop();
        reset();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            carduino = (CarduinodroidApplication) getApplication();
            driveLandscape();

            sound = new Sound();

            serialConnectionStatusChangeReceiver = new SerialConnectionDriveActivityStatusChangeReceiver();
            serialConnectionStatusChangeFilter = new IntentFilter(getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
            //registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter,getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION),null);

            serialDataRxReceiver = new SerialDataRxReceiver();
            serialDataRxFilter = new IntentFilter(getString(R.string.SERIAL_DATA_RX_RECEIVED));

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }catch (Exception e){
            Log.e(TAG,"onCreate()"+e.toString());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(serialConnectionStatusChangeReceiver, serialConnectionStatusChangeFilter);
        registerReceiver(serialDataRxReceiver, serialDataRxFilter);
        if(carduino.dataContainer.getSerialState().isRunning()) {
            refresh();
        }
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
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat).
            // It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            viewVideo.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LOW_PROFILE | //hide status bar
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | //API 19
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //API 32
        );
        }
    };
    private final Runnable showSystemUIRunable = (new Runnable() {
        @Override
        public void run() {
            showSystem();
        }
    });
    private final Runnable goStopRunnable = new Runnable() {
        @Override
        public void run() {
            uiStop();
        }
    };
    private final Runnable goDriveRunnable = new Runnable() {
        @Override
        public void run() {
            uiDrive();
        }
    };

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

    private void showSystem(){
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
        uiStop();
    }

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

    private void uiStop(){
        if(!carduino.dataContainer.preferences.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            viewDebug.setVisibility(View.GONE);
        }
        viewStop.setVisibility(View.VISIBLE);
        buttonDrive.setAlpha(1.0f);
        buttonHorn.setAlpha(1.0f);
        buttonFrontLight.setAlpha(1.0f);
        buttonStatusLed.setAlpha(1.0f);
    }
    private void uiDrive(){
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
                speed = (int) (scale((-1.0f)*mAngle1FilteredPitch,MAX_SPEED_DEGREE)*carduino.dataContainer.SPEED_MAX);
                break;
            case Surface.ROTATION_90:
                speed = (int) (scale(mAngle2FilteredRoll,MAX_SPEED_DEGREE)*carduino.dataContainer.SPEED_MAX);
                break;
            case Surface.ROTATION_180:
                speed = (int) (scale(mAngle1FilteredPitch,MAX_SPEED_DEGREE)*carduino.dataContainer.SPEED_MAX);
                break;
            default:
                speed = (int) (scale((-1.0f)*mAngle2FilteredRoll,MAX_SPEED_DEGREE)*carduino.dataContainer.SPEED_MAX);
                break;
        }

        textViewSpeed.setText(String.format(getString(R.string.driveSpeed), speed));
        seekBarSpeed.setProgress(speed + carduino.dataContainer.SPEED_MAX);
        carduino.dataContainer.setSpeed(speed);
    }

    private void setSteering(){
        int steering;
        switch (rotation) {
            case Surface.ROTATION_0:
                steering = (int) (scale(mAngle2FilteredRoll,MAX_STEER_DEGREE)*carduino.dataContainer.STEER_MAX);
                break;
            case Surface.ROTATION_90:
                steering = (int) (scale(mAngle1FilteredPitch,MAX_STEER_DEGREE)*carduino.dataContainer.STEER_MAX);
                break;
            case Surface.ROTATION_180:
                steering = (int) (scale((-1.0f)*mAngle2FilteredRoll,MAX_STEER_DEGREE)*carduino.dataContainer.STEER_MAX);
                break;
            default:
                steering = (int) (scale((-1.0f)*mAngle1FilteredPitch,MAX_STEER_DEGREE)*carduino.dataContainer.STEER_MAX);
                break;
        }
        textViewSteer.setText(String.format(getString(R.string.driveSteer), steering));
        seekBarSteer.setProgress(steering+carduino.dataContainer.STEER_MAX);
        carduino.dataContainer.setSteer(steering);
    }
}
