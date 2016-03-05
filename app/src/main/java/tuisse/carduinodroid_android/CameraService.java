package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.DataHandler;

/**
 * <h1>Camera Service</h1>
 * An important part of the CarduinoDroid Application is the background usage of the camera to
 * create a picture stream (some kind of MJPEG stream) because it has the smallest possible delay.
 * This service will instantiate the CameraControl if a Start Command is send and there is no
 * object.
 *
 * @author Lars Vogel
 * @version 1.0
 * @since 18.02.2016
 */
public class CameraService extends Service {

    static final String TAG = "CameraService";
    private CarduinodroidApplication carduino;
    private CameraControl cam;
    static private boolean isDestroyed = false;

    /**
     * This method provides access to the Carduino database
     * @return the CarduinodroidApplication as object to use the Data Handler
     */
    protected CarduinodroidApplication getCarduino(){

        return carduino;
    }

    /**
     * This method will give the option to get certain data of the CarduinoData Class
     * @return the CarduinoData as object to access its data base
     */
    private CarduinoData getData(){

        return carduino.dataHandler.getData();
    }

    /**
     * This method will give the option to get certain data of the CarduinoDroidData Class
     * @return the CarduinoDroidData as object to access its data base
     */
    private CarduinoDroidData getDData(){

        return carduino.dataHandler.getDData();
    }

    /**
     * This method is a part to get into the data of the Carduino(Droid)Data Class
     * @return the CarduinoData as object to access its data base
     */
    private DataHandler getDataHandler(){

        return carduino.dataHandler;
    }

    /**
     * This method returns the isDestroyed status.
     *
     * @return isDestroyed of the Camera Service
     */
    static synchronized boolean getIsDestroyed(){

        return isDestroyed;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        carduino = (CarduinodroidApplication) getApplication();
        Log.d(TAG, "onCreate");
    }

    /**
     * This method is used in all services and is needed at the start. It will create a new
     * CameraControl object to get access to all the important settings.
     * @param intent is given by the onStartCommand of a service
     * @param flags is given by the onStartCommand of a service
     * @param startId is given by the onStartCommand of a service
     * @return the kind how this service starts (START_STICKY, ...)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Camera Service onStartCommand");
        isDestroyed = false;

        if(cam==null) {
            cam = new CameraControl(this);

            new Thread(new Runnable() {
                public void run() {
                    if(cam!=null){
                        cam.init();
                        cam.start();
                    }

                }
            }, "CameraConnectionThread").start();
        }

        return START_STICKY;
    }

    /**
     * This method is called when the service is closed and the camera object needs to be destroyed
     */
    public void onDestroy() {
        super.onDestroy();
        if(cam != null)
            cam.close();
        isDestroyed = true;
        Log.i(TAG, "Camera Service has been killed");
    }
}
