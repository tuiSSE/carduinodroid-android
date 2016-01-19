package tuisse.carduinodroid_android;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

/**
 * Created by keX on 12.01.2016.
 */
public class Utils {

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
