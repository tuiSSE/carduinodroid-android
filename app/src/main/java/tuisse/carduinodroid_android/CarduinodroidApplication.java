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
    private SharedPreferences sharedPrefs;
    private SharedPreferences prefMain;

    protected DataContainer dataContainer = null;
    private static Context appContext = null;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        dataContainer = new DataContainer();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefMain = this.getSharedPreferences(getString(R.string.preference_main), this.MODE_PRIVATE);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        updateSharedPreferences();
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        dataContainer = null;
        appContext = null;
        Log.i(TAG, "onTerminated");
    }

    public synchronized int getIntPref(SharedPreferences prefs, String key, int defaultVal) {
        String value = prefs.getString(key, null);
        Log.d(TAG,"getIntPref: " +value);
        return value == null ? defaultVal : Integer.valueOf(value);
    }

    public synchronized void updateSharedPreferences() {
        if(dataContainer == null){
            Log.e(TAG,"no dataContainer initialized");
        }
        if (dataContainer.preferences == null){
            Log.d(TAG,"no preferences initialized");
            dataContainer.preferences = new Preferences();
        }

        dataContainer.preferences.setSerialPref(SerialType.fromInteger(getIntPref(sharedPrefs, "serialType", SerialType.toInteger(SerialType.BLUETOOTH))));
        dataContainer.preferences.setControlMode(ControlMode.fromInteger(sharedPrefs.getInt("controlMode", ControlMode.toInteger(ControlMode.TRANSCEIVER))));

        dataContainer.preferences.setBluetoothDeviceName(prefMain.getString("bluetoothDeviceName", getString(R.string.serialDefaultBluetoothDeviceName)));
        Log.d(TAG, "updating Prefs");

        Log.d(TAG,"serialType: " + dataContainer.preferences.getSerialPref().toString());
        Log.d(TAG,"bluetoothDeviceName: "+ dataContainer.preferences.getBluetoothDeviceName());
        Log.d(TAG,"controlMode: "+ dataContainer.preferences.getControlMode());
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSharedPreferences();
    }

    public static Context getAppContext(){
        if(appContext == null){
            //should never happen
            Log.e(TAG,"undefined app state: no app context");
        }
        return appContext;
    }
/*
    public static DataContainer getDataContainer(){
        if(dataContainer == null){
            //should never happen
            Log.e(TAG,"undefined app state: no dataContainer");
        }
        return dataContainer;
    }
*/
    public synchronized void setSharedPrefsInt(String key, int val){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, val);
        editor.apply();
    }
}
