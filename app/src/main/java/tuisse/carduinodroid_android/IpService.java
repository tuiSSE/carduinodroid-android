package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;

public class IpService extends Service {

    static final String TAG = "CarduinoIpService";
    static private boolean isDestroyed = false;

    private CarduinodroidApplication carduino;
    private IpConnection ip;

    private CarduinoDroidData getDData(){return carduino.dataHandler.getDData();}
    protected CarduinodroidApplication getCarduino(){return carduino;}

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
        // Starting the Service more then one time before closing can cause still cause some errors
        if(ip == null){
            if(!getDData().getIpState().isUnknown()){
                Log.i(TAG, "IP Connection not yet started but in the wrong mode");
                getDData().setIpState(new ConnectionState(ConnectionEnum.UNKNOWN, ""));
            }
        }else{
            if(!getDData().getIpState().isIdle() && isDestroyed){
                Log.i(TAG, "Service was closed earlier but IP Connection was still in use - Resetting");
                ip.close();
                ip = null;
                isDestroyed = false;
            }
        }

        if(getDData().getIpState().isUnknown()){
            Log.i(TAG, "Creating IP Connection");
            ip = new IpConnection(this);
            ip.init();
        }

        if (ip != null) {
            if (ip.isIdle()) {
                ip.startThread("CtrlSocket");
                if(!ip.isUnknown())
                    ip.startThread("DataSocket");
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        isDestroyed = true;
        super.onDestroy();
        if(ip != null){

            ip.close();
        }
        Log.i(TAG, "onDestroyed");
    }
}
