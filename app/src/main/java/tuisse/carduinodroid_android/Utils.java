package tuisse.carduinodroid_android;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.util.Log;

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
            Log.d(TAG, "getIntPref error, take defaultVal");
        }
        return value == null ? defaultVal : Integer.valueOf(value);
    }

    public static synchronized void setIntPref(String key, Integer val){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(CarduinodroidApplication.getAppContext()).edit();
        editor.putString(key, val.toString());
        editor.apply();
    }
}
