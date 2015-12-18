package tuisse.carduinodroid_android;

import android.app.Application;
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
    protected final int             TIMEOUT = 800;//800ms

    protected CarduinodroidApplication  carduino;
    protected SerialService             serialService;
    protected SerialState               serialState;
    protected String                    serialName;
    protected SerialSendThread          serialSendThread;
    protected SerialReceiveThread       serialReceiveThread;
    protected SerialType                serialType;

    public SerialConnection(Application a,SerialService s) {
        carduino = (CarduinodroidApplication) a;
        serialService = s;
        serialState = SerialState.IDLE;
        serialType = SerialType.NONE;
        serialName = "";
        serialSendThread      = new SerialSendThread();
        serialReceiveThread   = new SerialReceiveThread();
    }

    abstract protected boolean find();
    abstract protected boolean connect();
    abstract protected boolean close();
    abstract protected void send() throws IOException;
    abstract protected boolean receive() throws IOException;

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

    protected void setSerialState(SerialState state){
        if(state != serialState) {
            serialState = state;
            Log.d(TAG, "serial State changed: " + serialState.getStateName());
            Intent onSerialConnectionStatusChangeIntent = new Intent(carduino.getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
            onSerialConnectionStatusChangeIntent.putExtra(carduino.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_STATE), serialState.getStateName());
            onSerialConnectionStatusChangeIntent.putExtra(carduino.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_LOGO), serialState.getLogoId(serialType));
            onSerialConnectionStatusChangeIntent.putExtra(carduino.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_NAME), serialName);
            //carduino.sendBroadcast(onSerialConnectionStatusChangeIntent, carduino.getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION));
            carduino.sendBroadcast(onSerialConnectionStatusChangeIntent);
            serialService.showNotification();
        }
    }

    protected enum SerialType{
        NONE(0),BLUETOOTH(1),USB(2);
        SerialType(int t){
            type = t;
        }
        int type;

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
        IDLE(0), TRYCONNECT(1), CONNECTED(1), RUNNING(3), ERROR(-1), STREAMERROR(-2);
        SerialState(int s) {
            state = s;
        }
        private int state;
        public int getState() {
            return state;
        }
        public String getStateName(){
            String s = "";
            switch (state){
                case  0: s = "Idle";break;
                case  1: s = "Try to connect ...";break;
                case  2: s = "Connected";break;
                case  3: s = "Running";break;
                case -2: s = "Streamerror";break;
                default: s = "Error";break;
            }
            return s;
        }

        public int getLogoId(SerialType t){
            int logo = R.drawable.serial_idle;
            switch (state){
                case  1:    if(t == SerialType.BLUETOOTH)
                                logo = R.drawable.serial_bt_try_connect;
                            else
                                logo = R.drawable.serial_usb_try_connect;
                            break;
                case  2:
                case  3:    if(t == SerialType.BLUETOOTH)
                    logo = R.drawable.serial_bt_connected;
                else
                    logo = R.drawable.serial_usb_connected;
                    break;
                case -1:
                case -2: logo = R.drawable.serial_error;break;
                default: logo = R.drawable.serial_idle; break;
            }
            return logo;
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
                        if(receive()){
                            lastReceiveTime = System.currentTimeMillis();
                        }
                        if(lastReceiveTime + TIMEOUT < System.currentTimeMillis()){
                            Log.e(TAG, "Receive timed out");
                            serialName = "time out";
                            setSerialState(SerialState.STREAMERROR);
                            serialService.stopSelf();
                        }
                    } catch (java.io.IOException e){
                        Log.e(TAG, "Receive failed: " + e.toString());
                        serialName = "time out";
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
