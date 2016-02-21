package tuisse.carduinodroid_android;

import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.DataHandler;

/**
 * Created by Bird on 18.02.2016.
 */
public class CameraControl {

    static final String TAG = "CameraControl";
    private CameraService cameraService;
    private SurfaceHolder sHolder;

    protected PackageManager packageManager;
    protected Camera camera;
    protected Camera.Parameters parameters;
    protected List<Camera.Size> supportedPreviewSizes;
    protected int numSupportedPrevSizes;
    protected List<Camera.Size> supportedPictureSizes;
    protected List<Integer> supportedPreviewFPS;

    private int cameraID;
    private byte[] previewData;

    private int previewWidth;
    private int previewHeight;
    private int previewQuality;

    private boolean onDestroy;

    CameraControl(CameraService s){

        cameraService = s;
        init();
        onDestroy = false;
    }

    protected DataHandler getDataHandler(){

        return cameraService.getCarduino().dataHandler;
    }

    protected void init(){

        packageManager = cameraService.getPackageManager();

        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){

            cameraID = getBackFacingCameraID();

            if(cameraID > -1) {

                camera = Camera.open(cameraID);
                SurfaceView sView = new SurfaceView(cameraService.getApplicationContext());
                camera.startPreview();

                parameters = camera.getParameters();
                getSupportedPreviewSizes();
                numSupportedPrevSizes = supportedPreviewSizes.size();

                //previewHeight = supportedPreviewSizes.get(numSupportedPrevSizes-1).height;
                //previewWidth = supportedPreviewSizes.get(numSupportedPrevSizes-1).width;

                previewHeight = supportedPreviewSizes.get(1).height;
                previewWidth = supportedPreviewSizes.get(1).width;

                parameters.setPreviewSize(previewWidth, previewHeight);
                parameters.setJpegQuality(100);
                setCompressQuality(30);
                setOrientation(270);

                camera.setParameters(parameters);
                camera.setPreviewCallback(previewCallback);

                try {
                    camera.setPreviewDisplay(sView.getHolder());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                Log.e(TAG,"No Back Camera available on this mobile phone");
            }
        }else{
            Log.e(TAG,"No Camera available on this mobile phone");
        }
    }

    protected void close(){

        releaseCamera();
        camera = null;
    }

    private void releaseCamera(){
        //disable FlashLight
        setFlash(0);
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
    }

    private void setFlash(int status){
        if(status==1) activateFlash();
        else disableFlash();
    }

    private void activateFlash(){
        parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    private void disableFlash(){
        parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    private void setCameraType(String Type){
        //Front or Back Camera
    }

    private void setCameraResolution(){

    }

    private void setOrientation(int degree){

        parameters.setRotation(degree);
    }

    private void setFramesPerSec(){

    }

    private void setCompressQuality(int quality){

        if(quality > 100) previewQuality = 100;
        else if(quality < 0) previewQuality = 0;
        else previewQuality = quality;
    }

    private void setSupportedFPS(int ID){

    }

    private void getSupportedFPS(){

        supportedPreviewFPS = parameters.getSupportedPreviewFrameRates();
    }

    private void getSupportedPreviewSizes(){

        supportedPreviewSizes = parameters.getSupportedPreviewSizes();
    }

    private void getSupportedPictureSizes(){

        supportedPictureSizes = parameters.getSupportedPictureSizes();
    }

    private int getFrontFacingCameraID() {

        int ID=-1;
        int number = camera.getNumberOfCameras();

        for (int i = 0; i < number; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                Log.i(TAG, "Camera found");
                ID = i;
                break;
            }
        }

        return ID;
    }

    private int getBackFacingCameraID(){

        int ID=-1;
        int number = camera.getNumberOfCameras();

        for (int i = 0; i < number; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                Log.i(TAG, "Camera found");
                ID = i;
                break;
            }
        }

        return ID;
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        int i=0;
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            previewData = data.clone();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            YuvImage temp = new YuvImage(data, parameters.getPreviewFormat(), parameters.getPreviewSize().width,
                    parameters.getPreviewSize().height, null);
            Rect rect = new Rect(0, 0, parameters.getPreviewSize().width, parameters.getPreviewSize().height);
            temp.compressToJpeg(rect, previewQuality, baos);
            byte[] image = baos.toByteArray();

            getDData().setCameraPicture(image);

            Intent onCameraDataIntent = new Intent(Constants.EVENT.CAMERA_DATA_RECEIVED);
            LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onCameraDataIntent);
            Log.i(TAG, "Bild yes");
        }
    };

    protected synchronized CarduinoDroidData getDData(){

        return cameraService.getCarduino().dataHandler.getDData();
    }

    protected synchronized CarduinoData getData(){

        return cameraService.getCarduino().dataHandler.getData();
    }
}
