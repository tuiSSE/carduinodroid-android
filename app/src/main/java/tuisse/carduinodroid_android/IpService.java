package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataHandler;

public class IpService extends Service {

    static final String TAG = "CarduinoIpService";
    static private boolean isDestroyed = false;

    private CarduinodroidApplication carduino;
    private IpConnection ip;

    private CarduinoDroidData getDData(){return carduino.dataHandler.getDData();}
    private DataHandler getDataHandler(){return carduino.dataHandler;}
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
        // Still some issues but cant locate how they are going to start. in Debug everything is fine
        // Some Rare not really (re)producable scenarios causing only one (send or receive) data thread to work
        // Sometimes the SocketServer cant be created even with the Reuse
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
