package tuisse.carduinodroid_android;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

/**
 * Created by keX on 09.12.2015.
 */
abstract public class SerialConnection {
    private final String            TAG = "CarduinoSerial";
    protected final int             DELAY = 100;//100ms
    protected final int             HEARTBEAT = 100;

    protected CarduinodroidApplication  carduino;
    protected SerialService             serialService;
    protected SerialState               serialState;
    protected String                    serialName;
    protected SerialSendThread          serialSendThread;
    protected SerialReceiveThread       serialReceiveThread;

    public SerialConnection(Application a,SerialService s) {
        carduino = (CarduinodroidApplication) a;
        serialService = s;
        setSerialState(SerialState.IDLE);
        serialName = "";
        serialSendThread      = new SerialSendThread();
        serialReceiveThread   = new SerialReceiveThread();
    }

    abstract public boolean find();
    abstract public boolean connect();
    abstract public boolean close();
    abstract protected void send() throws IOException;
    abstract protected void receive() throws IOException;

    protected boolean reset() {
        setSerialState(SerialState.IDLE);
        serialSendThread.interrupt();
        serialReceiveThread.interrupt();
        return true;
    }

    protected void start() {
            setSerialState(SerialState.RUNNING);
            serialSendThread.start();
            serialReceiveThread.start();
    }

    public void setSerialState(SerialState state){
        if(state != serialState) {
            serialState = state;
            Log.d(TAG, "serial State changed: " + serialState.getStateName());
            Intent onSerialConnectionStatusChangeIntent = new Intent(carduino.dataContainer.intentStrings.SERIAL_CONNECTION_STATUS_CHANGED);
            onSerialConnectionStatusChangeIntent.putExtra(carduino.dataContainer.intentStrings.SERIAL_CONNECTION_STATUS_EXTRA_STATE, serialState.getStateName());
            onSerialConnectionStatusChangeIntent.putExtra(carduino.dataContainer.intentStrings.SERIAL_CONNECTION_STATUS_EXTRA_NAME, serialName);
            //carduino.sendBroadcast(onSerialConnectionStatusChangeIntent, carduino.dataContainer.intentStrings.SERIAL_CONNECTION_STATUS_PERMISSION);
            carduino.sendBroadcast(onSerialConnectionStatusChangeIntent);
        }
    }

    public boolean isIdle() {
        return serialState == SerialState.IDLE;
    }
    public boolean isConnected() {
        return serialState == SerialState.CONNECTED;
    }
    public boolean isError() {
        return (serialState == SerialState.ERROR) || (serialState == SerialState.STREAMERROR);
    }
    public boolean isTryConnect() {
        return serialState == SerialState.TRYCONNECT;
    }
    public boolean isRunning() {
        return serialState == SerialState.RUNNING;
    }
    public SerialState getSerialState() {
        return serialState;
    }

    protected enum SerialState {
        IDLE(0), CONNECTED(1), TRYCONNECT(2), RUNNING(3), ERROR(-1), STREAMERROR(-2);
        private int state;
        private SerialState(int s) {
            state = s;
        }
        public int getState() {
            return state;
        }

        public String getStateName(){
            String s = "";
            switch (state){
                case  0: s = "Idle";break;
                case  1: s = "Connected";break;
                case  2: s = "Try to connect ...";break;
                case  3: s = "Running";break;
                case -2: s = "Streamerror";break;
                default: s = "Error";break;
            }
            return s;
        }
    }

    protected class SerialReceiveThread extends Thread {

        public SerialReceiveThread() {
            super("SerialConnection-SerialReceiveThread");
        }

        @Override
        public void run() {
            int heartbeat = HEARTBEAT;
            Log.d(TAG,"SerialReceiveThread started");
            while (isRunning()) {
                try {
                    try{
                        receive();
                    } catch (java.io.IOException e){
                        Log.e(TAG, "Receive failed: " + e.toString());
                        serialName = "Connection timed out";
                        setSerialState(SerialState.STREAMERROR);
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if(heartbeat-- == 0){
                        heartbeat = HEARTBEAT;
                        Log.d(TAG,"pulse SerialReceive");
                    }
                } catch (InterruptedException e) {
                    setSerialState(SerialState.IDLE);
                    Log.d(TAG, "SerialReceiveThread stopped" + e.toString());
                    serialService.stopSelf();
                }
            }
        }
    }//SerialReceiveThread

    protected class SerialSendThread extends Thread {
        public SerialSendThread() {
            super("SerialConnection-SerialSendThread");
        }
        @Override
        public void run() {
            int heartbeat = HEARTBEAT;
            Log.d(TAG,"SerialSendThread started");
            while (isRunning()) {
                try {
                    //check actual connection
                    try {
                        send();
                    } catch (java.io.IOException e){
                        Log.e(TAG, "Send failed: " + e.toString());
                        serialName = "Connection timed out";
                        setSerialState(SerialState.STREAMERROR);
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if (heartbeat-- == 0) {
                        heartbeat = HEARTBEAT;
                        Log.d(TAG, "pulse SerialSend");
                    }
                } catch (InterruptedException e) {
                    setSerialState(SerialState.IDLE);
                    Log.d(TAG, "SerialSendThread stopped" + e.toString());
                    serialService.stopSelf();
                }
            }
        }
    }//SerialSendThread

}
