package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    public CarduinodroidApplication carduino;
    private SerialConnection serial;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        carduino = (CarduinodroidApplication) getApplication();
        serial = new SerialBluetooth(getApplication(),this);
        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(serial.isError()) {
            Log.e(TAG, "resetting serial");
            serial.reset();
        }
        if (!serial.isIdle()) {
            Log.d(TAG, "serial already started");
            return Service.START_STICKY;
        }

        carduino.setSerialServiceRunning(true);
        new Thread(new Runnable() {
            public void run() {
                serial.find();
                if(serial.connect()) {
                    serial.start();
                }
                else{
                    stopSelf();
                }
            }
        }, "connectSerialThread").start();
        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        serial.close();
        carduino.setSerialServiceRunning(false);
        //disconnect
        Log.i(TAG, "onDestroyed");
    }



}
