package tuisse.carduinodroid_android;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.DataHandler;

/**
 * <h1>Camera Control</h1>
 * An important part of the CarduinoDroid Application is the background usage of the camera to
 * create a picture stream (some kind of MJPEG stream) because it has the smallest possible delay.
 * This class is started by the service and gives access to all the important camera functions.
 *
 * @author Lars Vogel
 * @version 1.0
 * @since 18.02.2016
 */
public class CameraControl extends SurfaceView implements Camera.PreviewCallback, Runnable{

    static final String TAG = "CameraControl";

    private CameraService cameraService;
    protected PackageManager packageManager;
    protected Camera camera;
    protected Camera.Parameters parameters;
    private SurfaceTexture surfaceTexture;
    private int surfID;

    protected List<Camera.Size> supportedPreviewSizes;
    protected int numSupportedPrevSizes;

    private IntentFilter ipDataCameraFilter;
    private IpDataCameraReceiver ipDataCameraReceiver;

    private int cameraID;

    private String[] previewResolution;
    private int previewWidth;
    private int previewHeight;

    private int previewResolutionID;
    private int previewQuality;
    private int previewFlashLight;
    private int previewCamType;

    /**
     * This method gives access to the Data Handler (central data base)
     * @return the centralized Data Handler
     */
    protected DataHandler getDataHandler(){

        return cameraService.getCarduino().dataHandler;
    }

    /**
     * The constructor will create a Broadcast Receiver with its necessary Intent to listen to.
     * @param s is used as variable for the camera service to get access to certain functions
     */
    CameraControl(CameraService s){

        super(s);
        cameraService = s;

        ipDataCameraReceiver = new IpDataCameraReceiver();
        ipDataCameraFilter = new IntentFilter(Constants.EVENT.IP_DATA_CAMERA);
    }

    /**
     * This method is needed to initialize important camera parameters if a new connection between
     * transceiver and remote device is set up. It lowers the initial bandwidth especially in regions
     * with low speed.
     */
    protected void init(){

        surfID = 10;

        previewResolutionID = -1;
        previewQuality = 50;
        previewFlashLight = 0;
        previewCamType = 1;
    }

    /**
     * This method is used to establish new camera settings by getting all the saved information
     * out of the data base.
     */
    protected void update(){

        previewResolutionID = getDData().getCameraResolutionID();
        previewQuality = getDData().getCameraQuality();
        previewFlashLight = getDData().getCameraFlashlight();
        previewCamType = getDData().getCameraType();
    }

    /**
     * This method is called either a new connection is created between transceiver and remote
     * device or after the camera settings are changed. It checks if a camera is available on the
     * used mobile phone. Afterwards you need to check the camera object itself and which type is
     * chosen.
     * The most important part is described by the dummy SurfaceTexture to emulate a not-existing
     * activity because a Camera Preview in Android wants to show its pictures.
     * Then all setting are integrated and the camera Preview is started with a the created
     * PreviewCallback
     */
    protected void start() {

        packageManager = cameraService.getPackageManager();

        LocalBroadcastManager.getInstance(cameraService).registerReceiver(ipDataCameraReceiver, ipDataCameraFilter);

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

                    surfaceTexture = new SurfaceTexture(surfID);

                    try {
                        camera.setPreviewTexture(surfaceTexture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    parameters = camera.getParameters();

                    getSupportedPreviewSizes();
                    numSupportedPrevSizes = supportedPreviewSizes.size();

                    setCompressQuality(previewQuality);
                    setFlash(previewFlashLight);

                    setCameraResolution(previewResolutionID);

                    parameters.setPreviewSize(previewWidth, previewHeight);

                    camera.setParameters(parameters);

                    Thread thread = new Thread(this);
                    thread.start();

                }catch(Exception e){
                    Log.e(TAG,"Camera Service Blocked - usually restart Mobil Phone");
                }
            }
        }
    }

    /**
     * This methods enables the right close of the camera and unregister the BroadcastReceiver for
     * camera setting changes.
     */
    protected void close(){

        LocalBroadcastManager.getInstance(cameraService).unregisterReceiver(ipDataCameraReceiver);

        releaseCamera();
        camera = null;
    }

    /**
     * This method is part of the closing procedure to release the camera (disable flashlight) and
     * reset the PreviewCallback to stop taking picture
     */
    private void releaseCamera(){

        if(parameters != null)
            setFlash(0);
        if(camera!= null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        }
    }

    /**
     * This method set the status of the Flashlight
     * @param status enables the Flashlight with 1 and disables with 0
     */
    private void setFlash(int status){

        if(status==1)
            activateFlash();
        else
            disableFlash();
        previewFlashLight = status;
    }

    /**
     * This method activates the Flashlight by the Flash_Mode_Torch
     */
    private void activateFlash(){

        parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    /**
     * This method disables the Flashlight by the Flash_Mode_Off
     */
    private void disableFlash(){

        parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * This method sets the Camera Resolution out of the SupportedPreviewSize-Array by the ID
     * ( 0 is the highest possible resolution and n the lowest one). If it is starting with the
     * default parameter (-1) this method will overwrite it with the lowest possible resolution ID.
     * @param ID is the identifier for the Size-Array (0 highest to n lowest resolution)
     */
    private void setCameraResolution(int ID){

        if((ID <= (supportedPreviewSizes.size()-1)) && (ID >= 0)) {
            previewHeight = supportedPreviewSizes.get(ID).height;
            previewWidth = supportedPreviewSizes.get(ID).width;
            previewResolutionID = ID;
            getDData().setCameraResolutionID(previewResolutionID);
        }else if(ID == -1) {
            previewHeight = supportedPreviewSizes.get(supportedPreviewSizes.size()-1).height;
            previewWidth = supportedPreviewSizes.get(supportedPreviewSizes.size()-1).width;
            previewResolutionID = supportedPreviewSizes.size()-1;
            getDData().setCameraResolutionID(previewResolutionID);
        }else{
            Log.e(TAG,"Error on Setting a Resolution - ID is not in the expected Range");
        }
    }

    /**
     * An important part for a short delay is the definition of the compress Quality for the
     * picture stream.
     * @param quality is a value from 0 (low) to 100 (high)
     */
    private void setCompressQuality(int quality){

        if(quality > 100) previewQuality = 100;
        else if(quality < 0) previewQuality = 0;
        else previewQuality = quality;
    }

    /**
     * This method transforms the Supported Preview Sizes, divided into height and width, out of
     * the camera parameters into an String-Array with "width x height"
     */
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
        //Signal the IP Connection that we can send the resolution possibilties of transceiver
        Intent onSupportedResolutions = new Intent(Constants.EVENT.CAMERA_SUPPORTED_RESOLUTION);
        LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onSupportedResolutions);
    }

    /**
     * Depending on the used mobile phone it may happen, that the ID of Back or Front Camera may
     * differ. With the help of "Camera_Facing_Front" this method extracts the right ID.
     * @return ID of the Front Facing Camera
     */
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

    /**
     * Depending on the used mobile phone it may happen, that the ID of Back or Front Camera may
     * differ. With the help of "Camera_Facing_Back" this method extracts the right ID.
     * @return ID of the Back Facing Camera
     */
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

    /**
     * This methods is called when the BroadcastReceiver gets an Intent that new Camera Settings
     * were received. First it checks if the variables has been changed and depending on the
     * variable a certain process is used.
     * Update Resolution or Camera Type will trigger a restart of the camera object
     * Update Flashlight or Preview Quality just needs to set the values
     */
    private void updateCameraData(){

        boolean isUpdated = false;

        int _resolutionID = getDData().getCameraResolutionID(); //Width and Height
        int _cameraType = getDData().getCameraType();
        int _flashLight = getDData().getCameraFlashlight();
        int _cameraQuality = getDData().getCameraQuality();

        if(_resolutionID != previewResolutionID){
            if(!( _resolutionID == -1 && previewResolutionID == (numSupportedPrevSizes-1))) {
                isUpdated = true;
            }
        }

        if(_cameraType != previewCamType) isUpdated = true;

        if(isUpdated){
            close();
            update();
            start();
        }else{
            if(_flashLight != previewFlashLight)
                setFlash(_flashLight);
            if(_cameraQuality != previewQuality)
                setCompressQuality(_cameraQuality);
        }
    }

    /**
     * onPreviewFrame is an essential part of the Camera.PreviewCallback to create the picture
     * stream. It is an inner class and gives a picture as Byte-Array with selected camera settings
     * back. Each Picture starts an own Thread to handle the CPU and Memory hungry Compression.
     * @param data contains a full picture in a chosen resolution
     * @param camera contains all the predefined camera settings for the preview picture
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        new processImages(camera, data).start();
    }

    /**
     * The BroadcastReceiver is a tool to transform received data into camera setting changes
     * depending on the obtained information by calling the updateCameraData()-Method
     */
    private class IpDataCameraReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            updateCameraData();
        }
    }

    /**
     * This Runnable enables the onPreviewFrame but takes a certain amount of time. It prevents
     * a stucked application.
     */
    @Override
    public void run() {
        try {

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

    /**
     * ProcessImages describes a Thread to provide the compression of all created pictures by the
     * onPreviewFrame. As it is a performance hungry, the YUV picture with NV21 ImageFormat is used
     * because it should be part in all the current android versions.
      */
    public class processImages extends Thread {

        protected Camera camera;
        protected byte[] data;

        public processImages(Camera _camera, byte[] _data) {

            camera = _camera;
            data = _data;
        }

        public void run() {

            Camera.Parameters param = camera.getParameters();
            int width = param.getPreviewSize().width;
            int height = param.getPreviewSize().height;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Rect rect = new Rect(0, 0, width, height);
            YuvImage test = new YuvImage(data,ImageFormat.NV21,width,height,null);
            test.compressToJpeg(rect, 50, baos);

            byte[] image = baos.toByteArray();

            /*** Test for Rotation ***/
            //Not Working on really Huge Images ... We need to test whats the max Format to Rotate
            /*Matrix mat = new Matrix();
            mat.postRotate(degree);

            ByteArrayOutputStream rotationBaos = new ByteArrayOutputStream();

            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, mat, true);

            rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, rotationBaos);
            byte[] rotatedImage = rotationBaos.toByteArray();*/

            getDData().setCameraPicture(image);

            Log.i(TAG, "New picture data Available");

            Intent onCameraData = new Intent(Constants.EVENT.CAMERA_DATA_RECEIVED);
            LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onCameraData);
        }
    }

    /**
     * This method giving access to the CarduinoDroid database through the dataHandler
     * @return CarduinoDroid object for the Getter and Setter
     */
    protected synchronized CarduinoDroidData getDData(){

        return cameraService.getCarduino().dataHandler.getDData();
    }

    /**
     * This method giving access to the Carduino database through the dataHandler
     * @return Carduino object for the Getter and Setter
     */
    protected synchronized CarduinoData getData(){

        return cameraService.getCarduino().dataHandler.getData();
    }
}
