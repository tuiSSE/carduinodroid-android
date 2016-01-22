package tuisse.carduinodroid_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ChooseBluetoothDeviceActivity extends AppCompatActivity {
    private static final String TAG = "CarduinoChooseBTDevice";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_bluetooth_device_activity);
        Log.d(TAG, "onCreated ChooseBluetoothDeviceActivity");
    }
}
