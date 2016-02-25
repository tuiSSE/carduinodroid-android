package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.DataHandler;

public class CameraService extends Service {

    static final String TAG = "CameraService";
    private CarduinodroidApplication carduino;
    private CameraControl cam;
    static private boolean isDestroyed = false;

    protected CarduinodroidApplication getCarduino(){

        return carduino;
    }

    private CarduinoData getData(){

        return carduino.dataHandler.getData();
    }

    private CarduinoDroidData getDData(){

        return carduino.dataHandler.getDData();
    }

    private DataHandler getDataHandler(){

        return carduino.dataHandler;
    }

    /**
     * static return function for the isDestroyed status.
     *
     * @return isDestroyed
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

    public void onDestroy() {
        super.onDestroy();
        if(cam != null)
            cam.close();
        isDestroyed = true;
        Log.i(TAG, "Camera Service has been killed");
    }
}
