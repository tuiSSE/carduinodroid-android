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
public class CarduinodroidApplication extends Application /* implements SharedPreferences.OnSharedPreferenceChangeListener */{
    private static final String TAG = "CarduinoApplication";


    private SharedPreferences sharedPrefs;

    protected DataContainer dataContainer = null;
    private static Context appContext = null;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        dataContainer = new DataContainer();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateSharedPreferences(sharedPrefs, "initialize");
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dataContainer = null;
        appContext = null;
        Log.i(TAG, "onTerminated");
    }

    public synchronized void updateSharedPreferences(SharedPreferences sharedPreferences, String key) {
        if(dataContainer == null){
            Log.e(TAG,"no dataContainer initialized");
        }
        if (dataContainer.preferences == null){
            Log.d(TAG, "no preferences initialized");
            dataContainer.preferences = new Preferences();
        }
        Log.d(TAG, key + ": updating Prefs");
        switch(key){
            case "pref_key_serial_type":
                dataContainer.preferences.setSerialPref(SerialType.fromInteger(Utils.getIntPref(key, SerialType.toInteger(SerialType.BLUETOOTH))));
                Log.d(TAG, key + ": " + dataContainer.preferences.getSerialPref().toString());
                break;
            case "pref_key_control_mode":
                dataContainer.preferences.setControlMode(ControlMode.fromInteger(Utils.getIntPref(key, ControlMode.toInteger(ControlMode.TRANSCEIVER))));
                Log.d(TAG, key + ": " + dataContainer.preferences.getControlMode());
                break;
            case "pref_key_bluetooth_device_name":
                dataContainer.preferences.setBluetoothDeviceName(sharedPreferences.getString(key, getString(R.string.serialDefaultBluetoothDeviceName)));
                Log.d(TAG, key + ": " + dataContainer.preferences.getBluetoothDeviceName());
            default:
                dataContainer.preferences.setSerialPref(SerialType.fromInteger(Utils.getIntPref("pref_key_serial_type", SerialType.toInteger(SerialType.BLUETOOTH))));
                dataContainer.preferences.setControlMode(ControlMode.fromInteger(Utils.getIntPref("pref_key_control_mode", ControlMode.toInteger(ControlMode.TRANSCEIVER))));
                dataContainer.preferences.setBluetoothDeviceName(sharedPreferences.getString("pref_key_bluetooth_device_name", getString(R.string.serialDefaultBluetoothDeviceName)));
                Log.d(TAG, "pref_key_serial_type: "             + dataContainer.preferences.getSerialPref().toString());
                Log.d(TAG, "pref_key_control_mode: "            + dataContainer.preferences.getControlMode());
                Log.d(TAG, "pref_key_bluetooth_device_name: "   + dataContainer.preferences.getBluetoothDeviceName());
                break;
        }
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

}
