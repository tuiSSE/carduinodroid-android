package com.swp.tuilmenau.carduinodroid_android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by keX on 04.12.2015.
 */
public class CarduinodroidApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "CarduinoApplication";
    private SharedPreferences sharedPrefs;
    private boolean serialServiceRunning = false;
    public Prefs prefs;
    public SerialDataTx serialDataTx;
    public SerialDataRx serialDataRx;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().clear().commit();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

    public boolean isSerialServiceRunning() {
        return this.serialServiceRunning;
    }

    public void setSerialServiceRunning(boolean serialServiceRunning) {
        this.serialServiceRunning = serialServiceRunning;
    }

    public synchronized void updateSharedPreferences(){
        if(this.prefs == null)
            this.prefs = new Prefs();
        prefs.serialBluetooth1Usb0 = sharedPrefs.getInt("serialBluetooth1Usb0",1);
        prefs.rcNetwork1Activity0 = sharedPrefs.getBoolean("rcNetwork1Activity0",true);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSharedPreferences();
        Log.d(TAG,"updatePrefs");
    }
}
