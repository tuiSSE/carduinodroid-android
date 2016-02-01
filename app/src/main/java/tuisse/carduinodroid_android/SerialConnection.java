package tuisse.carduinodroid_android;

import android.content.Intent;
import android.util.Log;

import java.io.IOException;

import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataContainer;
import tuisse.carduinodroid_android.data.SerialData;

/**
 * Created by keX on 09.12.2015.
 */
abstract public class SerialConnection {
    private final String            TAG = "CarduinoSerialConn";
    protected final int             DELAY = 100;//100ms
    protected final int             HEARTBEAT = 100;
    protected final int             TIMEOUT = 1300;//800ms
    protected final int             RECEIVE_BUFFER_LENGTH = 30;

    protected SerialService             serialService;
    protected SerialSendThread          serialSendThread;
    protected SerialReceiveThread       serialReceiveThread;

    public SerialConnection(SerialService s) {
        serialService = s;
        serialSendThread      = new SerialSendThread();
        serialReceiveThread   = new SerialReceiveThread();
    }

    abstract protected boolean find();
    abstract protected boolean connect();
    abstract protected boolean close();
    abstract protected void send() throws IOException;
    abstract protected int receive() throws IOException;

    protected boolean reset() {
        setSerialState(ConnectionEnum.IDLE);
        if(serialSendThread.isAlive()){
            serialSendThread.interrupt();
        }
        serialReceiveThread.interrupt();
        return true;
    }

    protected void start() {
        setSerialState(ConnectionEnum.RUNNING);
        serialSendThread.start();
        serialReceiveThread.start();
    }

    protected synchronized void setSerialState(ConnectionEnum state){
        setSerialState(state, "");
    }

    protected synchronized void setSerialState(ConnectionEnum state, int errorId){
        setSerialState(state,serialService.getString(errorId));
    }

    protected synchronized void setSerialState(ConnectionEnum state, String error){
        if(getData().getSerialState() != null) {
            //if (!state.equals(getSerialData().getSerialState().getState())) {//if not equal
            getData().setSerialState(new ConnectionState(state, error));
                Log.d(TAG, "serial State changed: " + getData().getSerialState().getStateName());
                if(getData().getSerialState().isError()){
                    Log.e(TAG, error);
                }
                else if(!error.equals("")){
                    Log.w(TAG, error);
                }
                Intent onSerialConnectionStatusChangeIntent = new Intent(serialService.getCarduino().getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
                //onSerialConnectionStatusChangeIntent.putExtra(serialService.getCarduino().getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA), getSerialData().getSerialState().getState().ordinal());
                //serialService.getCarduino().sendBroadcast(onSerialConnectionStatusChangeIntent, serialService.getCarduino().getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION));

                if (serialService != null) {
                    serialService.sendBroadcast(onSerialConnectionStatusChangeIntent);
                    serialService.showNotification();
                }
            //}
        }
        else{
            Log.e(TAG,"no getSerialData()");
        }
    }

    protected class SerialReceiveThread extends Thread {
        public SerialReceiveThread() {
            super("SerialConnection-SerialReceiveThread");
        }
        @Override
        public void run() {
            int heartbeat = HEARTBEAT;
            Log.d(TAG, "SerialReceiveThread started");
            long lastReceiveTime = System.currentTimeMillis();
            while (isRunning()) {
                try {
                    try{
                        if(receive() > 0){
                            lastReceiveTime = System.currentTimeMillis();
                            Intent onSerialDataRxIntent = new Intent(serialService.getString(R.string.SERIAL_DATA_RX_RECEIVED));
                            serialService.sendBroadcast(onSerialDataRxIntent);
                        }
                        if(lastReceiveTime + TIMEOUT < System.currentTimeMillis()){
                            Log.e(TAG, serialService.getString(R.string.serialErrorReceiveTimeout));
                            setSerialState(ConnectionEnum.STREAMERROR,R.string.serialErrorReceiveTimeout);
                            serialService.stopSelf();
                        }
                    } catch (java.io.IOException e){
                        Log.e(TAG, String.format(serialService.getString(R.string.serialErrorReceiveFail),e.toString()));
                        setSerialState(ConnectionEnum.STREAMERROR, String.format(serialService.getString(R.string.serialErrorReceiveFail),e.toString()));
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if(heartbeat-- == 0){
                        heartbeat = HEARTBEAT;
                        Log.d(TAG,"pulse SerialReceive");
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionEnum.IDLE);
                    Log.d(TAG, "SerialReceiveThread stopped: " + e.toString());
                    serialService.stopSelf();
                }
            }
            setSerialState(serialService.getCarduino().dataContainer.getSerialState().getState());
            serialService.stopSelf();
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
                        Log.e(TAG, String.format(serialService.getString(R.string.serialErrorSendFail),e.toString()));
                        setSerialState(ConnectionEnum.STREAMERROR,String.format(serialService.getString(R.string.serialErrorSendFail),e.toString()));
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if (heartbeat-- == 0) {
                        heartbeat = HEARTBEAT;
                        Log.d(TAG, "pulse SerialSend");
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionEnum.IDLE);
                    Log.d(TAG, "SerialSendThread stopped: " + e.toString());
                    serialService.stopSelf();
                }
            }
            setSerialState(serialService.getCarduino().dataContainer.getSerialState().getState());
            serialService.stopSelf();
        }
    }//SerialSendThread

    protected synchronized boolean isIdle() {
        return getData().getSerialState().isIdle();
    }
    protected synchronized boolean isFound() {
        return getData().getSerialState().isFound();
    }
    protected synchronized boolean isConnected() {
        return getData().getSerialState().isConnected();
    }
    protected synchronized boolean isError() {
        return getData().getSerialState().isError();
    }
    protected synchronized boolean isTryConnect() {
        return getData().getSerialState().isTryConnect();
    }
    protected synchronized boolean isRunning() {
        return getData().getSerialState().isRunning();
    }
    protected synchronized boolean isUnknown() {
        return getData().getSerialState().isUnknown();
    }
    protected synchronized boolean isStarted(){
        return getData().getSerialState().isStarted();
    }

    protected synchronized DataContainer getData(){
        return serialService.getCarduino().dataContainer;
    }

}
