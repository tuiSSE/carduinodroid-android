package tuisse.carduinodroid_android;

import android.content.Context;

import android.hardware.Camera;
import android.media.ImageReader;

/**
 * Created by Bird on 18.02.2016.
 */
public class CameraControl {

    static final String TAG = "CameraControl";
    CameraService cameraService;

    protected Camera camera;
    protected Camera.Parameters parameters;

    CameraControl(CameraService s){

        cameraService = s;
        init();
    }

    protected void init(){

    }
}
