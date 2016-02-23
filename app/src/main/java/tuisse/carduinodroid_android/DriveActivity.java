package tuisse.carduinodroid_android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.CarduinoIF;
import tuisse.carduinodroid_android.data.DataHandler;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoDriveActivity";

    private CarduinodroidApplication carduino;

    private SerialDataRxReceiver serialDataRxReceiver;
    private CommunicationStatusChangeReceiver communicationStatusChangeReceiver;
    private IpDataRxReceiver ipDataRxReceiver;
    private CameraDataReceiver cameraDataReceiver;

    private IntentFilter serialDataRxFilter;
    private IntentFilter communicationStatusChangeFilter;
    private IntentFilter ipDataRxFilter;
    private IntentFilter cameraDataFilter;

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
    private ImageView viewImage;
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

    private Button buttonDrive;
    private Button buttonHorn;
    private Button buttonFrontLight;
    private Button buttonStatusLed;
    private Button buttonCameraSettings;

    private CarduinoData getData(){
        return getDataHandler().getData();
    }
    private CarduinoDroidData getDData() { return getDataHandler().getDData(); }
    private DataHandler getDataHandler(){
        return carduino.dataHandler;
    }

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
            getData().resetMotors();
            setDistance(progressbarDistanceFront, -1);
            setDistance(progressbarDistanceBack, -1);
            seekBarSpeed.setProgress(127);
            seekBarSteer.setProgress(127);

            textViewSpeed.setText(String.format(getString(R.string.driveSpeed), getData().getSpeed()));
            textViewSteer.setText(String.format(getString(R.string.driveSteer), getData().getSteer()));
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
                    getData().getUltrasoundFrontFloat()));
            textViewDistanceBack.setText(String.format(getString(R.string.driveDistanceBack),
                    getData().getUltrasoundBackFloat()));
            setDistance(progressbarDistanceFront, getData().getUltrasoundFront());
            setDistance(progressbarDistanceBack, getData().getUltrasoundBack());
            textViewAbsBattery.setText(String.format(getString(R.string.driveAbsoluteBattery),
                    getData().getAbsBattCapFloat()));
            textViewRelBattery.setText(String.format(getString(R.string.driveRelativeBattery),
                    getData().getRelBattCapFloat()));
            textViewCurrent.setText(String.format(getString(R.string.driveCurrent),
                    getData().getCurrentFloat()));
            textViewVoltage.setText(String.format(getString(R.string.driveVoltage),
                    getData().getVoltageFloat()));
            textViewTemperature.setText(String.format(getString(R.string.driveTemperature),
                    getData().getTemperatureFloat()));
        }catch (Exception e){
            Log.e(TAG,"refresh()"+e.toString());
        }
    }

    private void initView(){
        checkBoxFailsafeStop        = (CheckBox)        findViewById(R.id.checkBoxFailsafeStop);
        checkBoxOrientation         = (CheckBox)        findViewById(R.id.checkBoxOrientation);
        checkBoxDebug               = (CheckBox)        findViewById(R.id.checkBoxDebug);
        seekBarSpeed                = (VerticalSeekBar) findViewById(R.id.seekBarSpeed);
        seekBarSteer                = (SeekBar)         findViewById(R.id.seekBarSteer);
        progressbarDistanceFront    = (ProgressBar)     findViewById(R.id.progressbarDistanceFront);
        progressbarDistanceBack     = (ProgressBar)     findViewById(R.id.progressbarDistanceBack);

        textViewSpeed               = (TextView) findViewById(R.id.textViewSpeed);
        textViewSteer               = (TextView) findViewById(R.id.textViewSteer);
        textViewAngle0              = (TextView) findViewById(R.id.textViewAngle0);
        textViewAngle1              = (TextView) findViewById(R.id.textViewAngle1);
        textViewAngle2              = (TextView) findViewById(R.id.textViewAngle2);

        textViewDistanceFront       = (TextView) findViewById(R.id.textViewDistanceFront);
        textViewDistanceBack        = (TextView) findViewById(R.id.textViewDistanceBack);
        textViewAbsBattery          = (TextView) findViewById(R.id.textViewAbsBattery);
        textViewRelBattery          = (TextView) findViewById(R.id.textViewRelBattery);
        textViewCurrent             = (TextView) findViewById(R.id.textViewCurrent);
        textViewVoltage             = (TextView) findViewById(R.id.textViewVoltage);
        textViewTemperature         = (TextView) findViewById(R.id.textViewTemperature);

        buttonDrive                 = (Button) findViewById(R.id.buttonDrive);
        buttonHorn                  = (Button) findViewById(R.id.buttonHorn);
        buttonFrontLight            = (Button) findViewById(R.id.buttonFrontLight);
        buttonStatusLed             = (Button) findViewById(R.id.buttonStatusLed);
        buttonCameraSettings        = (Button) findViewById(R.id.buttonCameraSettings);

        if (!carduino.dataHandler.getControlMode().isTransceiver()) {
            //if Remote or Direct mode

            buttonDrive.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_drive));
            buttonHorn.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_horn));
            buttonFrontLight.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_front_light));
            buttonStatusLed.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_status_led));
            buttonCameraSettings.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_settings_camera));
        } else {
            //if Transceiver mode
            buttonDrive.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_grey, R.drawable.icon_drive));
            buttonHorn.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_grey, R.drawable.icon_horn));
            buttonFrontLight.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_grey, R.drawable.icon_front_light));
            buttonStatusLed.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_grey, R.drawable.icon_status_led));
            buttonCameraSettings.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_grey, R.drawable.icon_settings_camera));
        }

        viewDebug = findViewById(R.id.fullscreen_content_debug);
        viewImage = (ImageView) findViewById(R.id.fullscreen_content_video);
        viewDistance = findViewById(R.id.fullscreen_content_distance);
        viewStop = findViewById(R.id.fullscreen_content_stop);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewDebug.getLayoutParams();
        if (!carduino.dataHandler.getControlMode().isTransceiver()) {
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

        buttonCameraSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialogCamSettings = new Dialog(DriveActivity.this);

                dialogCamSettings.setContentView(R.layout.dialog_camera_settings);
                dialogCamSettings.setTitle("Camera Settings");

                final Spinner spinnerResolution = (Spinner) dialogCamSettings.findViewById(R.id.spinnerResolution);
                final Spinner spinnerDegree = (Spinner) dialogCamSettings.findViewById(R.id.spinnerDegree);
                final CheckBox checkBoxCameraType = (CheckBox) dialogCamSettings.findViewById(R.id.checkboxCameraType);
                final EditText editQuality = (EditText) dialogCamSettings.findViewById(R.id.editQuality);
                Button dialogButtonOK = (Button) dialogCamSettings.findViewById(R.id.buttonOK);
                Button dialogButtonCancel = (Button) dialogCamSettings.findViewById(R.id.buttonCancel);

                String[] itemsResolution;
                String[] itemsDegree;
                int cameraDegreeID;

                itemsDegree = Constants.CAMERA_VALUES.ORIENTATION_DEGREES;
                itemsResolution = getDData().getCameraSupportedSizes();

                ArrayAdapter<String> adapterResolution;
                adapterResolution = new ArrayAdapter<String>(DriveActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsResolution);
                spinnerResolution.setAdapter(adapterResolution);

                spinnerResolution.setSelection(getCameraResolutionID());

                if (getDData().getCameraType() == 0) checkBoxCameraType.setChecked(false);
                else checkBoxCameraType.setChecked(true);

                ArrayAdapter<String> adapterDegree;
                adapterDegree = new ArrayAdapter<String>(DriveActivity.this, android.R.layout.simple_spinner_dropdown_item, itemsDegree);
                spinnerDegree.setAdapter(adapterDegree);

                cameraDegreeID = getCameraDegreeID();
                if (cameraDegreeID < 0)
                    Log.e(TAG, "Error while checking the Custom Camera Orientation Degree");
                else spinnerDegree.setSelection(cameraDegreeID);

                editQuality.setText(String.valueOf(getDData().getCameraQuality()));

                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        dialogCamSettings.dismiss();
                    }
                });

                dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        setCameraResolutionID(spinnerResolution.getSelectedItemPosition());
                        setCameraType(checkBoxCameraType.isChecked());
                        setCameraDegree(spinnerDegree.getSelectedItem().toString());
                        setCameraQuality(editQuality.getText().toString());

                        dialogCamSettings.dismiss();
                    }
                });

                dialogCamSettings.show();
            }
        });

        buttonDrive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!carduino.dataHandler.getControlMode().isTransceiver()) {
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
                            buttonDrive.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_drive_press));
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            buttonDrive.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_drive));
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
                            getData().setStatusLed(1);
                            buttonStatusLed.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_status_led_press));
                        } else {
                            getData().setStatusLed(0);
                            buttonStatusLed.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_status_led));
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
                            getData().setFrontLight(1);
                            buttonFrontLight.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_front_light_press));
                        } else {
                            getData().setFrontLight(0);
                            buttonFrontLight.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_front_light));
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
                if (!carduino.dataHandler.getControlMode().isTransceiver()) {
                    //if Remote or Direct mode
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            buttonHorn.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_horn_press));
                            switch (carduino.dataHandler.getControlMode()) {
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
                        case MotionEvent.ACTION_CANCEL:
                            buttonHorn.setBackground(Utils.assembleDrawables(R.drawable.buttonshape_primary_light, R.drawable.icon_horn));
                            switch (carduino.dataHandler.getControlMode()) {
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

        checkBoxDebug.setChecked(carduino.dataHandler.getDebugView());
        checkBoxDebug.setOnClickListener(new CheckBox.OnClickListener() {

            @Override
            public void onClick(View v) {
                int val = carduino.dataHandler.toggleDebugView();
                Utils.setIntPref(getString(R.string.pref_key_debug_view), val);
            }
        });
        checkBoxFailsafeStop.setChecked(carduino.dataHandler.getFailSafeStopPref());
        if (!carduino.dataHandler.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            checkBoxFailsafeStop.setEnabled(true);
            checkBoxFailsafeStop.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int val = carduino.dataHandler.toggleFailSafeStopPref();
                    Utils.setIntPref(getString(R.string.pref_key_failsafe_stop), val);
                    getData().setFailSafeStop(val);
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

        seekBarSpeed.setMax(254);
        seekBarSteer.setMax(254);
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

            communicationStatusChangeReceiver = new CommunicationStatusChangeReceiver();
            communicationStatusChangeFilter = new IntentFilter(Constants.EVENT.COMMUNICATION_STATUS_CHANGED);

            serialDataRxReceiver = new SerialDataRxReceiver();
            serialDataRxFilter = new IntentFilter(Constants.EVENT.SERIAL_DATA_RECEIVED);

            ipDataRxReceiver = new IpDataRxReceiver();
            ipDataRxFilter = new IntentFilter(Constants.EVENT.IP_DATA_RECEIVED);

            cameraDataReceiver = new CameraDataReceiver();
            cameraDataFilter = new IntentFilter(Constants.EVENT.CAMERA_DATA_RECEIVED);

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
        LocalBroadcastManager.getInstance(this).registerReceiver(communicationStatusChangeReceiver, communicationStatusChangeFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(serialDataRxReceiver, serialDataRxFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ipDataRxReceiver, ipDataRxFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(cameraDataReceiver, cameraDataFilter);
        setBackgroundColor();
        if(getData().getSerialState().isRunning()) {
            refresh();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(communicationStatusChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serialDataRxReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ipDataRxReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cameraDataReceiver);
        reset();
    }

    private class IpDataRxReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            setClientValues();
            //refresh();
        }
    }

    private class SerialDataRxReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    private class CommunicationStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "communication status change event: " + getDataHandler().getCommunicationStatus());
            setBackgroundColor();
        }
    }

    private class CameraDataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try {//TODO: Anzeige nur bei Remote und bei Debug-On, Absicherung wenn Activity zu spät an ist

                int width = viewImage.getWidth();
                int height = viewImage.getHeight();

                byte[] image = getDData().getCameraPicture();
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

                viewImage.setImageBitmap(bitmap.createScaledBitmap(bitmap, width, height, false));
            }catch(Exception e){
                Log.e(TAG,"Error on setting the Video/Picture on Activity");
            }
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
            viewImage.setSystemUiVisibility(
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
        viewImage.setSystemUiVisibility(
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
        if(!carduino.dataHandler.getControlMode().isTransceiver()) {
            //if Remote or Direct mode
            viewDebug.setVisibility(View.GONE);
        }
        viewStop.setVisibility(View.VISIBLE);
        buttonDrive.setAlpha(1.0f);
        buttonHorn.setAlpha(1.0f);
        buttonFrontLight.setAlpha(1.0f);
        buttonStatusLed.setAlpha(1.0f);
        buttonCameraSettings.setAlpha(1.0f);
    }
    private void uiDrive(){
        viewStop.setVisibility(View.GONE);
        if(checkBoxDebug.isChecked()) {
            viewDebug.setVisibility(View.VISIBLE);
            if(!carduino.dataHandler.getControlMode().isDirect()) {
                buttonDrive.setAlpha(0.6f);
                buttonHorn.setAlpha(0.6f);
                buttonFrontLight.setAlpha(0.6f);
                buttonStatusLed.setAlpha(0.6f);
                buttonCameraSettings.setAlpha(0.6f);
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
                speed = (int) (scale((-1.0f)*mAngle1FilteredPitch,Constants.GESTURE_ANGLE.SPEED) * CarduinoIF.SPEED_MAX);
                break;
            case Surface.ROTATION_90:
                speed = (int) (scale(mAngle2FilteredRoll,Constants.GESTURE_ANGLE.SPEED) * CarduinoIF.SPEED_MAX);
                break;
            case Surface.ROTATION_180:
                speed = (int) (scale(mAngle1FilteredPitch,Constants.GESTURE_ANGLE.SPEED) * CarduinoIF.SPEED_MAX);
                break;
            default:
                speed = (int) (scale((-1.0f)*mAngle2FilteredRoll,Constants.GESTURE_ANGLE.SPEED) * CarduinoIF.SPEED_MAX);
                break;
        }

        textViewSpeed.setText(String.format(getString(R.string.driveSpeed), speed));
        seekBarSpeed.setProgress(speed + CarduinoIF.SPEED_MAX);
        getData().setSpeed(speed);
    }

    private void setSteering(){
        int steering;
        switch (rotation) {
            case Surface.ROTATION_0:
                steering = (int) (scale(mAngle2FilteredRoll,Constants.GESTURE_ANGLE.STEER) * CarduinoIF.STEER_MAX);
                break;
            case Surface.ROTATION_90:
                steering = (int) (scale(mAngle1FilteredPitch,Constants.GESTURE_ANGLE.STEER) * CarduinoIF.STEER_MAX);
                break;
            case Surface.ROTATION_180:
                steering = (int) (scale((-1.0f)*mAngle2FilteredRoll,Constants.GESTURE_ANGLE.STEER) * CarduinoIF.STEER_MAX);
                break;
            default:
                steering = (int) (scale((-1.0f)*mAngle1FilteredPitch,Constants.GESTURE_ANGLE.STEER) * CarduinoIF.STEER_MAX);
                break;
        }
        textViewSteer.setText(String.format(getString(R.string.driveSteer), steering));
        seekBarSteer.setProgress(steering + CarduinoIF.STEER_MAX);
        getData().setSteer(steering);
    }

    private void setBackgroundColor(){
        viewImage.setBackgroundColor(getResources().getColor(getDataHandler().getCommunicationStatusColor()));
    }

    private void setClientValues(){

        int speed = getData().getSpeed();
        int steering = getData().getSteer();

        textViewSpeed.setText(String.format(getString(R.string.driveSpeed), speed));
        seekBarSpeed.setProgress(speed + CarduinoIF.SPEED_MAX);
        textViewSteer.setText(String.format(getString(R.string.driveSteer), steering));
        seekBarSteer.setProgress(steering + CarduinoIF.STEER_MAX);
    }

    private void setCameraQuality(String quality){

        int value = Integer.parseInt(quality);

        if(value > 100) getDData().setCameraQuality(100);
        else if(value < 0) getDData().setCameraQuality(0);
        else getDData().setCameraQuality(value);
    }

    private void setCameraType(boolean isChecked){

        if(isChecked)
            getDData().setCameraType(1);
        else
            getDData().setCameraType(0);
    }

    private void setCameraDegree(String degree){

        int value = Integer.parseInt(degree);
        getDData().setCameraDegree(value);
    }

    private int getCameraDegreeID(){

        int ID = -1;
        int actualValue = getDData().getCameraDegree();
        String[] values = Constants.CAMERA_VALUES.ORIENTATION_DEGREES;

        for(int i = 0; i < values.length; i++) {
            if(String.valueOf(actualValue).equals(values[i]))
                ID = i;
        }
        return ID;
    }

    private void setCameraResolutionID(int ID){

        getDData().setCameraResolutionID(ID);
    }

    private int getCameraResolutionID(){

        return getDData().getCameraResolutionID();
    }
}
