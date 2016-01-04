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
    //private static Context context = null;
    protected DataContainer dataContainer;
    private SharedPreferences sharedPrefs;


    @Override
    public void onCreate() {
        super.onCreate();
        dataContainer = new DataContainer();
        //context = getApplicationContext();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //deleting shared prefs needs to be cleared in a more stable VERSION:
        sharedPrefs.edit().clear().apply();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        stopService(new Intent(this, SerialService.class));
        dataContainer = null;
        //context = null;
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

    public synchronized void updateSharedPreferences() {
        if (dataContainer.preferences == null)
            dataContainer.preferences = new Preferences();
        dataContainer.preferences.serialBluetooth1Usb0 = sharedPrefs.getInt("serialBluetooth1Usb0", 1);
        dataContainer.preferences.rcNetwork1Activity0 = sharedPrefs.getBoolean("rcNetwork1Activity0", true);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSharedPreferences();
        Log.d(TAG, "updatePrefs");
    }

    /*
    protected Context getAppContext(){
        return CarduinodroidApplication.context;
    }
    */
}
