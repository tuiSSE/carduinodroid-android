package com.swp.tuilmenau.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    private final int DELAY = 100*10; //100*100ms
    private boolean mIsRunning = false;
    private boolean runFlag = false;
    private RunRxThread runRxThread;
    private RunTxThread runTxThread;
    private CarduinodroidApplication carduino;
    private SerialBluetooth bluetooth;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.carduino = (CarduinodroidApplication) getApplication();
        this.runRxThread = new RunRxThread();
        this.runTxThread = new RunTxThread();
        bluetooth = new SerialBluetooth();
        Log.d(TAG,"onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(runFlag == true){
            return Service.START_STICKY;
        }
        this.runFlag = true;
        this.carduino.setSerialServiceRunning(true);

        bluetooth.find();

        new Thread(new Runnable(){
            boolean connected = false;
            public void run(){
                while(!connected && runFlag)
                try {
                    Thread.sleep(DELAY);
                    connected = bluetooth.connect();
                } catch (InterruptedException e) {
                }
            }
        }, "connectBluetoothThread").start();
/*
        this.runRxThread.start();
        this.runTxThread.start();
*/
        Log.d(TAG, "onStarted");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        this.runFlag = false;
        this.carduino.setSerialServiceRunning(false);
        this.runRxThread.interrupt();
        this.runTxThread.interrupt();
        this.runRxThread = null;
        this.runTxThread = null;

        //disconnect
        Log.i(TAG,"onDestroyed");
    }

    private class RunRxThread extends Thread {

        public RunRxThread() {
            super("SerialService-RunRxThread");
        }

        @Override
        public void run() {
            SerialService serialService = SerialService.this;
            while (serialService.runFlag) {

                try {

                    Log.d(TAG, "runRxThread done");
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    serialService.runFlag = false;
                }
            }
        }
    }//runRxThread

    private class RunTxThread extends Thread {

        public RunTxThread() {
            super("SerialService-RunTxThread");
        }

        @Override
        public void run() {
            SerialService serialService = SerialService.this;
            while (serialService.runFlag) {

                try {

                    Log.d(TAG, "runTxThread done");
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    serialService.runFlag = false;
                }
            }
        }
    }//runRxThread

}
