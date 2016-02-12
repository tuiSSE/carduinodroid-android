package tuisse.carduinodroid_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import tuisse.carduinodroid_android.data.BluetoothHandling;
import tuisse.carduinodroid_android.data.DataHandler;
import tuisse.carduinodroid_android.data.ControlMode;
import tuisse.carduinodroid_android.data.SerialType;

/**
 * Created by keX on 04.12.2015.
 */
public class CarduinodroidApplication extends Application /* implements SharedPreferences.OnSharedPreferenceChangeListener */{
    private static final String TAG = "CarduinoApplication";

    private SharedPreferences sharedPrefs;

    protected DataHandler dataHandler = null;
    private static Context appContext = null;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        dataHandler = new DataHandler();
        Log.d(TAG, "dataHandler ready");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateSharedPreferences(sharedPrefs, "initialize");
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dataHandler = null;
        appContext = null;
        Log.i(TAG, "onTerminated");
    }

    public synchronized void updateSharedPreferences(SharedPreferences sharedPreferences, String key) {
        if(dataHandler == null){
            Log.e(TAG, "no dataHandler initialized");
        }
        Log.d(TAG, key + ": updating Prefs");
        switch(key){
            case "pref_key_serial_type":
                dataHandler.setSerialPref(SerialType.fromInteger(Utils.getIntPref(key, SerialType.toInteger(SerialType.BLUETOOTH))));
                Log.d(TAG, key + ": " + dataHandler.getSerialPref().toString());
                break;
            case "pref_key_control_mode":
                dataHandler.setControlMode(ControlMode.fromInteger(Utils.getIntPref(key, ControlMode.toInteger(ControlMode.TRANSCEIVER))));
                Log.d(TAG, key + ": " + dataHandler.getControlMode());
                break;
            case "pref_key_screensaver":
                dataHandler.setScreensaver(Utils.getIntPref(key, 60000));
                Log.d(TAG, key + ": " + dataHandler.getScreensaver());
                break;
            case "pref_key_bluetooth_device_name":
                dataHandler.setBluetoothDeviceName(sharedPreferences.getString(key, getString(R.string.serialDefaultBluetoothDeviceName)));
                Log.d(TAG, key + ": " + dataHandler.getBluetoothDeviceName());
                break;
            case "pref_key_bluetooth_handling":
                dataHandler.setBluetoothHandling(BluetoothHandling.fromInteger(Utils.getIntPref(key, BluetoothHandling.toInteger(BluetoothHandling.AUTO))));
                Log.d(TAG, key + ": " + dataHandler.getBluetoothHandling());
                break;
            case "pref_key_failsafe_stop":
                if(Utils.getIntPref(key,1) == 1){
                    dataHandler.setFailSafeStopPref(true);
                }
                else {
                    dataHandler.setFailSafeStopPref(false);
                }
                Log.d(TAG, key + ": " + dataHandler.getFailSafeStopPref());
                break;
            case "pref_key_debug_view":
                if(Utils.getIntPref(key,0) == 1){
                    dataHandler.setDebugView(true);
                }
                else {
                    dataHandler.setDebugView(false);
                }
                Log.d(TAG, key + ": " + dataHandler.getDebugView());
                break;
            default:
                dataHandler.setControlMode(ControlMode.fromInteger(Utils.getIntPref("pref_key_control_mode", ControlMode.toInteger(ControlMode.TRANSCEIVER))));
                dataHandler.setSerialPref(SerialType.fromInteger(Utils.getIntPref("pref_key_serial_type", SerialType.toInteger(SerialType.BLUETOOTH))));
                dataHandler.setBluetoothDeviceName(sharedPreferences.getString("pref_key_bluetooth_device_name", getString(R.string.serialDefaultBluetoothDeviceName)));
                dataHandler.setBluetoothHandling(BluetoothHandling.fromInteger(Utils.getIntPref("pref_key_bluetooth_handling", BluetoothHandling.toInteger(BluetoothHandling.AUTO))));
                if(Utils.getIntPref("pref_key_failsafe_stop",1) == 1){
                    dataHandler.setFailSafeStopPref(true);
                }
                else {
                    dataHandler.setFailSafeStopPref(false);
                }
                if(Utils.getIntPref("pref_key_debug_view",0) == 1){
                    dataHandler.setDebugView(true);
                }
                else {
                    dataHandler.setDebugView(false);
                }
                dataHandler.setScreensaver(Utils.getIntPref("pref_key_screensaver", 60000));
                Log.d(TAG, "pref_key_screensaver: " + dataHandler.getScreensaver());
                Log.d(TAG, "pref_key_failsafe_stop: " + dataHandler.getFailSafeStopPref());
                Log.d(TAG, "pref_key_debug_view: " + dataHandler.getDebugView());
                Log.d(TAG, "pref_key_bluetooth_handling: " + dataHandler.getBluetoothHandling());
                Log.d(TAG, "pref_key_serial_type: " + dataHandler.getSerialPref());
                Log.d(TAG, "pref_key_control_mode: " + dataHandler.getControlMode());
                Log.d(TAG, "pref_key_bluetooth_device_name: " + dataHandler.getBluetoothDeviceName());
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

}
