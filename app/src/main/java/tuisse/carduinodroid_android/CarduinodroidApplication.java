package tuisse.carduinodroid_android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by keX on 04.12.2015.
 */
public class CarduinodroidApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "CarduinoApplication";
    protected DataContainer dataContainer;
    private SharedPreferences sharedPrefs;


    @Override
    public void onCreate() {
        super.onCreate();
        dataContainer = new DataContainer();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateSharedPreferences();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        dataContainer = null;
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

    public synchronized int getIntPref(SharedPreferences prefs,String key, int def) {
        String value = prefs.getString(key, null);
        return value == null ? def : Integer.valueOf(value);
    }

    public synchronized void updateSharedPreferences() {
        if (dataContainer.preferences == null){
            Log.d(TAG,"no preferences initialized");
            dataContainer.preferences = new Preferences();
        }
        dataContainer.preferences.setSerialPref(SerialType.fromInteger(getIntPref(sharedPrefs,"serialType", 0)));
        dataContainer.preferences.setBluetoothDeviceName(sharedPrefs.getString("bluetoothDeviceName", getString(R.string.defaultBluetoothDeviceName)));
        dataContainer.preferences.rcNetwork1Activity0 = sharedPrefs.getBoolean("rcNetwork1Activity0", true);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "updating Prefs");
        updateSharedPreferences();
        Log.d(TAG, "updated Prefs");
    }
}
