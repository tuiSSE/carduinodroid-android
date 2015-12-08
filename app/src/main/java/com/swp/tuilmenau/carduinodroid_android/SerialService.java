package com.swp.tuilmenau.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    private final int DELAY = 100*10; //100*100ms

    private boolean runFlag = false;
    private RunThread runThread;
    private CarduinodroidApplication carduino;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.carduino = (CarduinodroidApplication) getApplication();
        this.runThread = new RunThread();
        Log.d(TAG,"onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.runFlag = true;
        this.runThread.start();
        this.carduino.setSerialServiceRunning(true);
        Log.d(TAG, "onStarted");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        this.runFlag = false;
        this.carduino.setSerialServiceRunning(false);
        this.runThread.interrupt();
        this.runThread = null;

        Log.d(TAG,"onDestroyed");
    }

    private class RunThread extends Thread {

        public RunThread() {
            super("SerialService-RunThread");
        }

        @Override
        public void run() {
            SerialService serialService = SerialService.this;
            while (serialService.runFlag) {
                Log.d(TAG, "runThread runs");
                try {

                    Log.d(TAG, "runThread done");
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    serialService.runFlag = false;
                }
            }
        }
    }//runThread

}
