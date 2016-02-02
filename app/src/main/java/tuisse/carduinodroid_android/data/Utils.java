package tuisse.carduinodroid_android.data;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import tuisse.carduinodroid_android.CarduinodroidApplication;

/**
 * Created by mate on 01.02.2016.
 */
public class Utils {
    static public String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            hexString.append(byteToHexString(b));
        }
        return hexString.toString();
    }

    static public synchronized String byteToHexString(byte b){
        StringBuffer hexString = new StringBuffer();
        int intVal = b & 0xff;
        if (intVal < 0x10)
            hexString.append("0");
        hexString.append(Integer.toHexString(intVal));
        hexString.append(" ");
        return hexString.toString();
    }

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
}
