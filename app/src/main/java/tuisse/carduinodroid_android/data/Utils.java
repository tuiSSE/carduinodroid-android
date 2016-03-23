package tuisse.carduinodroid_android.data;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import tuisse.carduinodroid_android.CarduinodroidApplication;

/**
 * <h1>Utils static class</h1>
 * utility class has static functions and no variables. every function is not dependent on this
 * class. It is available to the packet data.
 *
 * @author Till Max Schwikal
 * @since 01.02.2016
 * @version 1.0
 */
public class Utils {
    /**
     * helper function to convert a byte array in a readable hex-base string
     * @param array input byte array to be printed
     * @return printable hex string
     */
    static public String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            hexString.append(byteToHexString(b));
        }
        return hexString.toString();
    }

    /**
     * helper function to convert a single byte into a hex string
     * @param b byte to be converted
     * @return printable hex string
     */
    static public synchronized String byteToHexString(byte b){
        StringBuffer hexString = new StringBuffer();
        int intVal = b & 0xff;
        if (intVal < 0x10)
            hexString.append("0");
        hexString.append(Integer.toHexString(intVal));
        hexString.append(" ");
        return hexString.toString();
    }

    /**
     * helper function to overlay darawables from image resources
     * @param drawableBack background image resource
     * @param drawableFront foreground image resource
     * @return an composed drawable
     */
    public static LayerDrawable assembleDrawables(int drawableBack,int drawableFront){
        Drawable[] layers = new Drawable[2];
        layers[0] = CarduinodroidApplication.getAppContext().getResources().getDrawable(drawableBack);
        layers[1] = CarduinodroidApplication.getAppContext().getResources().getDrawable(drawableFront);
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }
}
