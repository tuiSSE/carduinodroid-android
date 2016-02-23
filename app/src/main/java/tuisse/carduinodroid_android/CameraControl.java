package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLES20;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

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
public class CameraControl extends SurfaceView implements Camera.PreviewCallback, Runnable{

    static final String TAG = "CameraControl";
    private CameraService cameraService;
    private Context context;
    private SurfaceHolder sHolder;

    protected PackageManager packageManager;
    protected Camera camera;
    protected Camera.Parameters parameters;
    protected List<Camera.Size> supportedPreviewSizes;
    protected int numSupportedPrevSizes;
    protected List<Integer> supportedPreviewFPS;

    private IpDataRxReceiver ipDataRxReceiver;
    private IntentFilter ipDataRxFilter;

    private SurfaceTexture surfaceTexture;

    private int cameraID;
    private String[] previewResolution;
    private int previewResolutionID;
    private int previewWidth;
    private int previewHeight;
    private int previewQuality;
    private int previewOrtientation;
    private int previewFlashLight;
    private int previewCamType;

    CameraControl(CameraService s){
        super(s);

        cameraService = s;

        init();
        start();
    }

    protected DataHandler getDataHandler(){

        return cameraService.getCarduino().dataHandler;
    }

    protected void init(){

        previewResolutionID = 5;
        previewQuality = 50;
        previewOrtientation = 0;
        previewFlashLight = 0;
        previewCamType = 1;
    }

    protected void update(){

        previewResolutionID = getDData().getCameraResolutionID();
        previewQuality = getDData().getCameraQuality();
        previewOrtientation = getDData().getCameraDegree();
        previewFlashLight = getDData().getCameraFlashlight();
        previewCamType = getDData().getCameraType();
    }

    protected void start() {

        ipDataRxReceiver = new IpDataRxReceiver();
        ipDataRxFilter = new IntentFilter(Constants.EVENT.IP_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(cameraService).registerReceiver(ipDataRxReceiver, ipDataRxFilter);

        packageManager = cameraService.getPackageManager();

        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){

            if(camera != null)
                camera.release();

            if(previewCamType==1)
                cameraID = getBackFacingCameraID();
            else
                cameraID = getFrontFacingCameraID();

            if(cameraID > -1) {
                try{
                    camera = Camera.open(cameraID);

                    //surfaceTexture = new SurfaceTexture(textID); textID mit Zähler?
                    surfaceTexture = new SurfaceTexture(10);

                    try {
                        camera.setPreviewTexture(surfaceTexture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    parameters = camera.getParameters();

                    getSupportedPreviewSizes();
                    numSupportedPrevSizes = supportedPreviewSizes.size();
                    //Set at beginning on lowest Resolution to get best Bandwidth
                    setCameraResolution(previewResolutionID);
                    //parameters.setPreviewSize(previewWidth, previewHeight);
                    setCompressQuality(previewQuality);
                    setOrientation(previewOrtientation);
                    setFlash(previewFlashLight);

                    parameters.setPreviewSize(640,480);

                    camera.setParameters(parameters);

                    Thread thread = new Thread(this);
                    thread.start();

                }catch(Exception e){
                    Log.e(TAG,"Camera Service Blocked - usually restart Mobil Phone");
                }
            }
        }
    }

    protected void close(){

        LocalBroadcastManager.getInstance(cameraService).unregisterReceiver(ipDataRxReceiver);

        releaseCamera();
        camera = null;

    }

    private void releaseCamera(){

        if(parameters != null)
            setFlash(0);
        if(camera!= null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
    }

    private void setFlash(int status){

        if(status==1) activateFlash();
        else disableFlash();
        previewFlashLight = status;
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

    private void setCameraResolution(int ID){

        previewHeight = supportedPreviewSizes.get(ID).height;
        previewWidth = supportedPreviewSizes.get(ID).width;

        parameters.setPreviewSize(previewWidth, previewHeight);
        //parameters.setPictureSize(previewWidth, previewHeight);
        previewResolutionID = ID;
    }

    private void setOrientation(int degree){

        parameters.setRotation(degree);
        previewOrtientation = degree;
    }

    //TODO: Try to integrate FPS?

    private void setCompressQuality(int quality){

        if(quality > 100) previewQuality = 100;
        else if(quality < 0) previewQuality = 0;
        else previewQuality = quality;
    }

    private void getSupportedPreviewSizes(){

        supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        int numValues = supportedPreviewSizes.size();
        previewResolution = new String[numValues];

        for(int i = 0; i < numValues; i++){
            int width = supportedPreviewSizes.get(i).width;
            int height = supportedPreviewSizes.get(i).height;

            previewResolution[i] = String.valueOf(width) + " x " + String.valueOf(height);
        }
        getDData().setCameraSupportedSizes(previewResolution);
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
        previewCamType = 0;
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
        previewCamType = 1;
        return ID;
    }

    private void updateCameraData(){

        boolean isUpdated = false;
        int _resolutionID = getDData().getCameraResolutionID(); //Width and Height
        int _cameraType = getDData().getCameraType();
        int _cameraDegree = getDData().getCameraDegree();
        int _flashLight = getDData().getCameraFlashlight();
        int _cameraQuality = getDData().getCameraQuality();

        if(_resolutionID != previewResolutionID) isUpdated = true;
        if(_cameraDegree != previewOrtientation) isUpdated = true;
        if(_flashLight != previewFlashLight) isUpdated = true;
        if(_cameraQuality != previewQuality) isUpdated = true;
        if(_cameraType != previewCamType) isUpdated = true;

        if(isUpdated){
            close();
            update();
            start();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Parameters param = camera.getParameters();
        int width = param.getPreviewSize().width;
        int height = param.getPreviewSize().height;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, width, height);
        YuvImage test = new YuvImage(data,ImageFormat.NV21,width,height,null);
        test.compressToJpeg(rect,50,baos);

        byte[] image = baos.toByteArray();

        getDData().setCameraPicture(image);

        Log.i(TAG, "BILD Yes");

        Intent onCameraDataIntent = new Intent(Constants.EVENT.CAMERA_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onCameraDataIntent);
    }

    @Override
    public void run() {
        try {
            // Add the first callback buffer to the queue
            camera.setPreviewCallback(this);

            Log.i(TAG,"setting camera callback with buffer");
        } catch (SecurityException e) {
            Log.e(TAG,"security exception, check permissions in your AndroidManifest to access to the camera",e);
        } catch (Exception e) {
            Log.e(TAG,"error adding callback",e);
        }

        try{
            camera.startPreview();

        } catch (Exception e) {
            Log.e(TAG,"error starting preview",e);
        }
    }

    private class IpDataRxReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            updateCameraData();
        }
    }

    protected synchronized CarduinoDroidData getDData(){

        return cameraService.getCarduino().dataHandler.getDData();
    }

    protected synchronized CarduinoData getData(){

        return cameraService.getCarduino().dataHandler.getData();
    }
}
