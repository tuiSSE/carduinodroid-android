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
public class CameraControl extends SurfaceView implements Camera.PreviewCallback, Camera.PictureCallback, Runnable, TextureView.SurfaceTextureListener{

    static final String TAG = "CameraControl";
    private CameraService cameraService;
    private Context context;
    private SurfaceHolder sHolder;

    protected PackageManager packageManager;
    protected Camera camera;
    protected Camera.Parameters parameters;
    protected List<Camera.Size> supportedPreviewSizes;
    protected int numSupportedPrevSizes;
    protected List<Camera.Size> supportedPictureSizes;
    protected List<Integer> supportedPreviewFPS;

    private IpDataRxReceiver ipDataRxReceiver;
    private IntentFilter ipDataRxFilter;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextureView mTextureView;
    private SurfaceTexture surfaceTexture;
    byte[] callbackBuffer;

    private int cameraID;
    private byte[] previewData;
    public int textureBuffer[];
    public byte gBuffer[];
    private byte[][] buffer = {null, null};

    private String[] previewResolution;
    private int previewResolutionID;
    private int previewWidth;
    private int previewHeight;
    private int previewQuality;
    private int previewOrtientation;
    private int previewFlashLight;
    private int previewCamType;
    private boolean isTakingPicture;

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

        ipDataRxReceiver = new IpDataRxReceiver();
        ipDataRxFilter = new IntentFilter(Constants.EVENT.IP_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(cameraService).registerReceiver(ipDataRxReceiver, ipDataRxFilter);

        //previewResolutionID = getDData().getCameraResolutionID();
        previewResolutionID = 0;
        previewQuality = getDData().getCameraQuality();
        previewOrtientation = getDData().getCameraDegree();
        previewFlashLight = getDData().getCameraFlashlight();
        previewCamType = getDData().getCameraType();
    }

    protected void start() {

        /*packageManager = cameraService.getPackageManager();

        if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){

            if(previewCamType==1) cameraID = getBackFacingCameraID();
            else cameraID = getFrontFacingCameraID();

            if(cameraID > -1) {

                //camera = Camera.open(cameraID);

                /*try {
                    camera.setPreviewTexture(new SurfaceTexture(10));
                } catch (IOException e1) {
                    Log.e(TAG, "OHOHOHOH");
                }*/

        //surfaceView = new SurfaceView(cameraService);
        //surfaceHolder = surfaceView.getHolder();

                /*surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder){
                        camera = Camera.open(cameraID);

                        try {
                            camera.setPreviewDisplay(holder);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        CamCallback camCallback = new CamCallback();
                        camera.setPreviewCallback(camCallback);
                        camera.startPreview();
                    }
                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder){}
                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}
                });*/
                /*surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                //mHolder.setSizeFromLayout();

                parameters = camera.getParameters();
                getSupportedPreviewSizes();
                numSupportedPrevSizes = supportedPreviewSizes.size();

                parameters.setJpegQuality(100);

                setCameraResolution(previewResolutionID);
                setCompressQuality(previewQuality);
                setOrientation(previewOrtientation);
                setFlash(previewFlashLight);

                camera.setParameters(parameters);

                /*try {
                    camera.setPreviewDisplay(surfaceHolder);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //camera.setPreviewCallback(previewCallback);
                //camera.startPreview();


            }else{
                Log.e(TAG,"No Back Camera available on this mobile phone");
            }
        }else{
            Log.e(TAG,"No Camera available on this mobile phone");
        }*/

        //parameters = camera.getParameters();



        //setCameraResolution(0);
        //int textures[] = new int[1];

        /*GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        int width  = 1920; // size of preview
        int height = 1080;  // size of preview
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
                height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);*/

        //texture.setDefaultBufferSize(4,4);

        /*int texture_id = textures[0];
        SurfaceTexture surfaceTexture = new SurfaceTexture(texture_id);

        camera = Camera.open();

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e1) {
            e1.printStackTrace();
        }*/

        /*Camera.Size previewSize= camera.getParameters().getPreviewSize();
        int dataBufferSize=(int)(previewSize.height*previewSize.width*
                (ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat())/8.0));
        callbackBuffer = new byte[dataBufferSize];*/

        /*camera.addCallbackBuffer(callbackBuffer);
        camera.setPreviewCallback(this);
        camera.startPreview();*/

        /*int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        int width  = 1920; // size of preview
        int height = 1080;  // size of preview
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
                height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        int textureId = textures[0];

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture
                .setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(
                            SurfaceTexture surfaceTexture) {

                                Log.i(TAG,"TESTTEST");
                    }
                });

        //Rect rect = new Rect(0, 0, 1920, 1080);

        camera = Camera.open();
        parameters = camera.getParameters();
        getSupportedPreviewSizes();
        parameters.setPreviewSize(640, 480);
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);
        camera.startPreview();*/

        /*int bufferSize = 1920 * 1080;
        textureBuffer = new int[bufferSize];
        bufferSize = bufferSize * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
        gBuffer = new byte[bufferSize];*/

        //camera.addCallbackBuffer(gBuffer);
        //camera.setPreviewCallbackWithBuffer(this);

        //isTakingPicture = false;
        //while (true) {
        //    if (!isTakingPicture){
        //        isTakingPicture = true;
         //       camera.startPreview();
         //       camera.takePicture(null,this,null);
        //    }
        //}

        /**** Test für alle Geräte Ergebnis: S1 - Bild Top / S6 - Bild doppelt = Matrix Problem*/
        if(camera != null) camera.release();

        camera = Camera.open();
        surfaceTexture = new SurfaceTexture(10);

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        parameters = camera.getParameters();
        parameters.setPictureSize(640, 480);
        parameters.setPreviewSize(640, 480);
        //parameters.setPreviewFormat(ImageFormat.YUY2);
        camera.setParameters(parameters);

        /*int bufferSize = 640 * 480;
        if(buffer == null || buffer.length != bufferSize) {
            // it actually needs (width*height) * 3/2
            bufferSize = bufferSize * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            buffer[0] = new byte[bufferSize];
            buffer[1] = new byte[bufferSize];
        }*/

        Thread thread = new Thread(this);
        thread.start();

        /**** Test für onPicture Mode mit wenig FPS - für alle Devices funktionsfähig
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        Rect rect = new Rect(0, 0, 1920, 1080);

        camera = Camera.open();
        parameters = camera.getParameters();
        getSupportedPreviewSizes();

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parameters.setPictureSize(640,480);
        camera.setParameters(parameters);
        camera.startPreview();
        camera.takePicture(null,this,null);*/
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
        supportedPictureSizes = parameters.getSupportedPictureSizes();

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
            init();
            start();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(gBuffer);
        previewData = data.clone();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        YuvImage temp = new YuvImage(data, parameters.getPreviewFormat(), 640,
                480, null);
        Rect rect = new Rect(0, 0, 640, 480);
        temp.compressToJpeg(rect, previewQuality, baos);
        byte[] image = baos.toByteArray();

        getDData().setCameraPicture(image);

        Log.i(TAG, "BILD Yes");

        Intent onCameraDataIntent = new Intent(Constants.EVENT.CAMERA_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onCameraDataIntent);
        isTakingPicture=false;
        //camera.addCallbackBuffer(gBuffer);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        //Possible but really slow with around 2 FPS
        Log.i(TAG, "Picture Taken");
        previewData = data.clone();
        getDData().setCameraPicture(previewData);

        Intent onCameraDataIntent = new Intent(Constants.EVENT.CAMERA_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(cameraService).sendBroadcast(onCameraDataIntent);
        isTakingPicture=false;

        //camera.stopPreview();
        //start();

        close();
        init();
        start();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG,"Available");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG,"SizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG,"Destroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG,"Updated");
    }

    @Override
    public void run() {
        try {
            // Add the first callback buffer to the queue
            camera.addCallbackBuffer(buffer[0]);
            camera.setPreviewCallback(this);

            Log.i("OF","setting camera callback with buffer");
        } catch (SecurityException e) {
            Log.e("OF","security exception, check permissions in your AndroidManifest to access to the camera",e);
        } catch (Exception e) {
            Log.e("OF","error adding callback",e);
        }

        try{
            camera.startPreview();

        } catch (Exception e) {
            Log.e("OF","error starting preview",e);
        }
    }

    private class CamCallback implements Camera.PreviewCallback{
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
        }
    }

    /*Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
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
        }
    };*/

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
