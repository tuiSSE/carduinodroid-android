package tuisse.carduinodroid_android;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.io.IOException;

/**
 * Created by keX on 09.12.2015.
 */
abstract public class SerialConnection {
    private final String            TAG = "CarduinoSerial";
    protected final int             DELAY = 100;//100ms
    protected final int             HEARTBEAT = 10;

    protected CarduinodroidApplication carduino;
    protected SerialState           serialState;
    protected String                serialName;
    protected SerialSendThread      serialSendThread;
    protected SerialReceiveThread   serialReceiveThread;

    public SerialConnection(Application a) {
        carduino = (CarduinodroidApplication) a;
        serialState = SerialState.IDLE;
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
        serialState = SerialState.IDLE;
        serialSendThread.interrupt();
        serialReceiveThread.interrupt();
        return true;
    }

    protected void start() {
            serialState = SerialState.RUNNING;
            serialSendThread.start();
            serialReceiveThread.start();
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
                        serialState = SerialState.STREAMERROR;
                    }
                    heartbeat--;
                    Thread.sleep(DELAY);
                    if(heartbeat == 0){
                        heartbeat = HEARTBEAT;
                        Log.d(TAG,"pulse SerialReceive");
                    }
                } catch (InterruptedException e) {
                    serialState = SerialState.IDLE;
                    Log.d(TAG,"SerialReceiveThread stopped" +e.toString());
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
                        serialState = SerialState.STREAMERROR;
                    }
                    heartbeat--;
                    Thread.sleep(DELAY);
                    if (heartbeat == 0) {
                        heartbeat = HEARTBEAT;
                        Log.d(TAG, "pulse SerialSend");
                    }
                } catch (InterruptedException e) {
                    serialState = SerialState.IDLE;
                    Log.d(TAG,"SerialSendThread stopped" + e.toString());
                }
            }
        }
    }//SerialSendThread

}
