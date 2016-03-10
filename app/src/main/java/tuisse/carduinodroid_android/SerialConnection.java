package tuisse.carduinodroid_android;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataHandler;

/**
 * <h1>Serial Connection abstract class</h1>
 * This abstract class defines the basic functions and implements serial send and receive threads.
 *
 * @author Till Max Schwikal
 * @version 1.0
 * @since 09.12.2015
 *
 * @see SerialBluetooth
 * @see SerialUsb
 * @see SerialService
 */
abstract public class SerialConnection {
    private final String            TAG = "CarduinoSerialConn";
    protected final int             RECEIVE_BUFFER_LENGTH = 30;

    protected SerialService             serialService;
    protected SerialSendThread          serialSendThread;
    protected SerialReceiveThread       serialReceiveThread;

    public SerialConnection(SerialService s) {
        serialService = s;
        serialSendThread      = new SerialSendThread();
        serialReceiveThread   = new SerialReceiveThread();
    }

    /**
     * function to find a proper carduino arduino
     * is called in serial service
     * @return true if found was successful
     */
    abstract protected boolean find();

    /**
     * function to connect to a proper carduino arduino
     * is called in serial service
     * @return true if connect was successful
     */
    abstract protected boolean connect();

    /**
     * function to close a serial connection
     * is called in serial service
     * @return true on proper closing
     */
    abstract protected boolean close();

    /**
     * function to send a serial frame
     * @throws IOException
     */
    abstract protected void send() throws IOException;

    /**
     * function to receive a serial frame
     * @return number of received bytes
     * @throws IOException
     */
    abstract protected int receive() throws IOException;

    protected DataHandler getDataHandler(){
        return serialService.getCarduino().dataHandler;
    }

    /**
     * resets the serial connection state
     * is called in serial service
     */
    protected void reset() {
        setSerialState(ConnectionEnum.IDLE);
        if(serialSendThread.isAlive()){
            serialSendThread.interrupt();
        }
        if(serialReceiveThread.isAlive()) {
            serialReceiveThread.interrupt();
        }
    }

    /**
     * starts the serial send and receive threads
     * is called in serial service
     */
    protected void start() {
        if(isConnected()) {
            setSerialState(ConnectionEnum.RUNNING);
            serialSendThread.start();
            serialReceiveThread.start();
        }
    }

    /**
     * overloaded function to set a serial state
     * @param state connection enum of the state
     */
    protected synchronized void setSerialState(ConnectionEnum state){
        setSerialState(state, "");
    }

    /**
     * overloaded function to set a serial state
     * @param state connection enum of the state
     * @param errorId error/description id string resource
     */
    protected synchronized void setSerialState(ConnectionEnum state, int errorId){
        setSerialState(state,serialService.getString(errorId));
    }

    /**
     * overloaded function to set a serial state
     * @param state connection enum of the state
     * @param error error/description string
     */
    protected synchronized void setSerialState(ConnectionEnum state, String error){
        if(getData().getSerialState() != null) {

            if (!(  state.equals(getData().getSerialState().getState()) &&
                    error.equals(getData().getSerialState().getError()))) {//if not equal
                getData().setSerialState(new ConnectionState(state, error));
                Log.d(TAG, "serial State changed: " + getData().getSerialState().getStateName());
                if(getData().getSerialState().isError()){
                    Log.e(TAG, error);
                }
                else if(!error.equals("")){
                    Log.w(TAG, error);
                }
            }
            if (serialService != null) {
                Intent onSerialConnectionStatusChangeIntent = new Intent(Constants.EVENT.SERIAL_STATUS_CHANGED);
                LocalBroadcastManager.getInstance(serialService).sendBroadcast(onSerialConnectionStatusChangeIntent);
            }
        }
        else{
            Log.e(TAG,"no getSerialData()");
        }
    }

    /**
     * serial receive thread
     * runs while serial state is RUNNING
     * receives serial messages and calls receive()
     */
    protected class SerialReceiveThread extends Thread {
        public SerialReceiveThread() {
            super("SerialConnection-SerialReceiveThread");
        }
        @Override
        public void run() {
            int heartbeat = Constants.HEART_BEAT.SERIAL;
            Log.d(TAG, "SerialReceiveThread started");
            long lastReceiveTime = System.currentTimeMillis();
            while (isRunning()) {
                try {
                    try{
                        if(receive() > 0){
                            lastReceiveTime = System.currentTimeMillis();
                            Intent onSerialDataRxIntent = new Intent(Constants.EVENT.SERIAL_DATA_RECEIVED);
                            LocalBroadcastManager.getInstance(serialService).sendBroadcast(onSerialDataRxIntent);
                        }
                        if(lastReceiveTime + Constants.TIMEOUT.SERIAL < System.currentTimeMillis()){
                            Log.e(TAG, serialService.getString(R.string.serialErrorReceiveTimeout));
                            setSerialState(ConnectionEnum.STREAMERROR,R.string.serialErrorReceiveTimeout);
                            serialService.stopSelf();
                        }
                    } catch (java.io.IOException e){
                        Log.e(TAG, String.format(serialService.getString(R.string.serialErrorReceiveFail),e.toString()));
                        setSerialState(ConnectionEnum.STREAMERROR, String.format(serialService.getString(R.string.serialErrorReceiveFail),e.toString()));
                        serialService.stopSelf();
                    }
                    Thread.sleep(Constants.DELAY.SERIAL);
                    if(heartbeat > 0) {
                        if (heartbeat-- == 0) {
                            heartbeat = Constants.HEART_BEAT.SERIAL;
                            Log.d(TAG, "pulse SerialReceive");
                        }
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionEnum.IDLE);
                    Log.d(TAG, "SerialReceiveThread stopped: " + e.toString());
                    serialService.stopSelf();
                }
            }
            setSerialState(getData().getSerialState().getState());
            serialService.stopSelf();
        }
    }//SerialReceiveThread

    /**
     * serial send thread
     * runs while serial connection state is RUNNING
     * sends serial messages and calls send()
     */
    protected class SerialSendThread extends Thread {
        public SerialSendThread() {
            super("SerialConnection-SerialSendThread");
        }
        @Override
        public void run() {
            int heartbeat = Constants.HEART_BEAT.SERIAL;
            Log.d(TAG,"SerialSendThread started");
            while (isRunning()) {
                try {
                    //check actual connection
                    try {
                        send();
                        //reset Accumulated Current
                        if(getData().getResetAccCur() == 1){
                            getData().setResetAccCur(0);
                            Utils.setIntPref(serialService.getString(R.string.pref_key_reset_battery), 0);
                        }
                    } catch (java.io.IOException e){
                        Log.e(TAG, String.format(serialService.getString(R.string.serialErrorSendFail),e.toString()));
                        setSerialState(ConnectionEnum.STREAMERROR,String.format(serialService.getString(R.string.serialErrorSendFail),e.toString()));
                        serialService.stopSelf();
                    }
                    Thread.sleep(Constants.DELAY.SERIAL);
                    if(heartbeat > 0) {
                        if (heartbeat-- == 0) {
                            heartbeat = Constants.HEART_BEAT.SERIAL;
                            Log.d(TAG, "pulse SerialSend");
                        }
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionEnum.IDLE);
                    Log.d(TAG, "SerialSendThread stopped: " + e.toString());
                    serialService.stopSelf();
                }
            }
            setSerialState(getData().getSerialState().getState());
            serialService.stopSelf();
        }
    }//SerialSendThread

    protected synchronized boolean isIdle() {
        return getData().getSerialState().isIdle();
    }
    protected synchronized boolean isFound() {
        return getData().getSerialState().isFound();
    }
    protected synchronized boolean isConnected() { return getData().getSerialState().isConnected();}
    protected synchronized boolean isError() {
        return getData().getSerialState().isError();
    }
    protected synchronized boolean isTryConnect() { return getData().getSerialState().isTryConnect();}
    protected synchronized boolean isRunning() {
        return getData().getSerialState().isRunning();
    }
    protected synchronized boolean isUnknown() {
        return getData().getSerialState().isUnknown();
    }
    protected synchronized boolean isStarted(){
        return getData().getSerialState().isStarted();
    }

    protected synchronized CarduinoData getData(){
        return serialService.getCarduino().dataHandler.getData();
    }
}
