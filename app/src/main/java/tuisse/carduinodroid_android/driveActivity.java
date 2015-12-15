package tuisse.carduinodroid_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class driveActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoDriveActivity";
    private CarduinodroidApplication carduino;

    private Button   buttonReset;
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
        carduino.dataContainer.serialDataTx.reset();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        this.carduino = (CarduinodroidApplication) getApplication();
        buttonReset             = (Button)   findViewById(R.id.buttonReset);
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
                carduino.dataContainer.serialDataTx.setStatusLed(val);
            }
        });

        checkBoxFrontLight.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxStatus.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialDataTx.setFrontLight(val);
            }
        });

        checkBoxResetAccCurrent.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxStatus.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialDataTx.setResetAccCur(val);
            }
        });

        checkBoxFailsafeStop.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                if(checkBoxStatus.isChecked()){
                    val = 1;
                }
                carduino.dataContainer.serialDataTx.setFailSafeStop(val);
            }
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSpeed.setText(String.format(getString(R.string.speed), (progress - 127)));
                carduino.dataContainer.serialDataTx.setSpeed(progress - 127);
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
                carduino.dataContainer.serialDataTx.setSteer(progress - 127);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
