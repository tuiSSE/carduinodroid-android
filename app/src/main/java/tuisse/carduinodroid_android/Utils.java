package tuisse.carduinodroid_android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by keX on 12.01.2016.
 */
public class Utils {
    private static final String TAG = "CarduinoUtils";

    public static LayerDrawable assembleDrawables(int drawableBack,int drawableFront){
        Drawable[] layers = new Drawable[2];
        layers[0] = CarduinodroidApplication.getAppContext().getResources().getDrawable(drawableBack);
        layers[1] = CarduinodroidApplication.getAppContext().getResources().getDrawable(drawableFront);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }
    public static LayerDrawable assembleDrawables(int drawableBack){
        Drawable[] layers = new Drawable[1];
        layers[0] = CarduinodroidApplication.getAppContext().getResources().getDrawable(drawableBack);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }

    public static synchronized int getIntPref(String key, int defaultVal) {
        String value = PreferenceManager.getDefaultSharedPreferences(CarduinodroidApplication.getAppContext()).getString(key, null);
        if(value == null){
            Log.d(TAG, key + " getIntPref error, take defaultVal: " + defaultVal);
        }
        return value == null ? defaultVal : Integer.valueOf(value);
    }

    public static synchronized void setIntPref(String key, Integer val){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CarduinodroidApplication.getAppContext()).edit();
        editor.putString(key, val.toString());
        editor.apply();
    }

    public static int lockScreenOrientation(Activity activity)
    {
        WindowManager windowManager =  (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Configuration configuration = activity.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        // Search for the natural position of the device
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) ||
                configuration.orientation == Configuration.ORIENTATION_PORTRAIT &&
                        (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270))
        {
            // Natural position is Landscape
            switch (rotation)
            {
                case Surface.ROTATION_0:
                    Log.d(TAG,"landscape");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case Surface.ROTATION_90:
                    Log.d(TAG,"reverse portrait");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_180:
                    Log.d(TAG,"reverse landscape");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case Surface.ROTATION_270:
                    Log.d(TAG,"portrait");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
            }
        }
        else
        {
            // Natural position is Portrait
            switch (rotation)
            {
                case Surface.ROTATION_0:
                    Log.d(TAG,"portrait");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Surface.ROTATION_90:
                    Log.d(TAG,"landscape");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case Surface.ROTATION_180:
                    Log.d(TAG,"reverse portrait");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_270:
                    Log.d(TAG,"reverse landscape");
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
            }
        }
        return rotation;
    }

    public static int setScreenOrientation(Activity activity, int requestedOrientation){
        int rotation;
        if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            rotation = Surface.ROTATION_90;
        }
        else{
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            rotation = Surface.ROTATION_0;
        }
        return rotation;
    }


    public static void unlockScreenOrientation(Activity activity)
    {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
