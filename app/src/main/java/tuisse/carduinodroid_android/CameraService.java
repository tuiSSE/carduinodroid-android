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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

        if(cam==null) cam = new CameraControl(this);

        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();

        cam.close();
        Log.i(TAG, "Camera Service has been killed");
    }
}
