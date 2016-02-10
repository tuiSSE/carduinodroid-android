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
import tuisse.carduinodroid_android.data.DataHandler;

public class IpService extends Service {

    static final String TAG = "CarduinoIpService";

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

        if(getDData().getIpState().isUnknown()){
            ip = new IpConnection(this);
            ip.init();
        }

        if(getDData().getIpState().isError()){
            Log.i(TAG, "FATAL ERROR - IP Connection will be restarted");
            if(ip !=null) ip.close();
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
        super.onDestroy();
        if(ip != null){

            ip.close();
        }
        Log.i(TAG, "onDestroyed");
    }
}
