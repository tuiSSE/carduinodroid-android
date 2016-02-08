package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IpService extends Service {

    static final String TAG = "CarduinoIpService";

    private CarduinodroidApplication carduino;
    private IpConnection ip;
    static private boolean isDestroyed = true;
    protected CarduinodroidApplication getCarduino(){
        return carduino;
    }

    static public boolean getIsDestroyed(){
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
        isDestroyed = false;

        if(ip == null){

            ip = new IpConnection(this);
            ip.init();
        }

        if (ip != null) {
            new Thread(new Runnable() {
                public void run() {

                    if(ip != null) {

                        ip.startThread("CtrlSocket");
                        //ip.closeThreads();
                        //Log.d(TAG, "New Init");
                        //ip.init();
                    }

                    if(isDestroyed){
                        stopSelf();
                    }
                }
            }, "connectIpCtrlThread").start();

            new Thread(new Runnable() {
                public void run() {

                    if(ip != null)

                        ip.startThread("DataSocket");


                    if(isDestroyed){
                        stopSelf();
                    }
                }
            }, "connectIpDataThread").start();

        }


        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        if(ip != null){

            ip.closeSocketServer();
            //ip = null;
        }
        isDestroyed=true;
        Log.i(TAG, "onDestroyed");
    }
}
