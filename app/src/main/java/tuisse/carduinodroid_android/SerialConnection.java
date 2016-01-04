package tuisse.carduinodroid_android;

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

    protected SerialService             serialService;
    protected SerialSendThread          serialSendThread;
    protected SerialReceiveThread       serialReceiveThread;

    public SerialConnection(SerialService s) {
        serialService = s;
        serialSendThread      = new SerialSendThread();
        serialReceiveThread   = new SerialReceiveThread();
    }

    protected SerialData getSerialData(){
        return serialService.getCarduino().dataContainer.serialData;
    }

    abstract protected boolean find();
    abstract protected boolean connect();
    abstract protected boolean close();
    abstract protected void send() throws IOException;
    abstract protected int receive() throws IOException;

    protected boolean reset() {
        setSerialState(ConnectionState.IDLE);
        serialSendThread.interrupt();
        serialReceiveThread.interrupt();
        return true;
    }

    protected void start() {
        setSerialState(ConnectionState.RUNNING);
        serialSendThread.start();
        serialReceiveThread.start();
    }

    public boolean isIdle() {
        return getSerialData().getConnectionState() == ConnectionState.IDLE;
    }
    public boolean isConnected() {
        return getSerialData().getConnectionState() == ConnectionState.CONNECTED;
    }
    public boolean isError() {
        return (getSerialData().getConnectionState() == ConnectionState.ERROR) || (getSerialData().getConnectionState() == ConnectionState.STREAMERROR);
    }
    public boolean isTryConnect() {
        return getSerialData().getConnectionState() == ConnectionState.TRYCONNECT;
    }
    public boolean isRunning() {
        return getSerialData().getConnectionState() == ConnectionState.RUNNING;
    }
    public ConnectionState getSerialState() {
        return getSerialData().getConnectionState();
    }

    protected void setSerialState(ConnectionState state){
        if(state != getSerialData().getConnectionState()) {
            getSerialData().setConnectionState(state);
            Log.d(TAG, "serial State changed: " + getSerialData().getConnectionState().getStateName());
            Intent onSerialConnectionStatusChangeIntent = new Intent(serialService.getCarduino().getString(R.string.SERIAL_CONNECTION_STATUS_CHANGED));
            onSerialConnectionStatusChangeIntent.putExtra(serialService.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_STATE), getSerialData().getConnectionState().getStateName());
            onSerialConnectionStatusChangeIntent.putExtra(serialService.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_LOGO), getSerialData().getLogoId());
            onSerialConnectionStatusChangeIntent.putExtra(serialService.getString(R.string.SERIAL_CONNECTION_STATUS_EXTRA_NAME), getSerialData().getSerialName());
            //serialService.getCarduino().sendBroadcast(onSerialConnectionStatusChangeIntent, serialService.getCarduino().getString(R.string.SERIAL_CONNECTION_STATUS_PERMISSION));
            serialService.sendBroadcast(onSerialConnectionStatusChangeIntent);
            serialService.showNotification();
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
                            Log.e(TAG, "Receive timed out");
                            getSerialData().setSerialName("time out");
                            setSerialState(ConnectionState.STREAMERROR);
                            serialService.stopSelf();
                        }
                    } catch (java.io.IOException e){
                        Log.e(TAG, "Receive failed: " + e.toString());
                        getSerialData().setSerialName("time out");
                        setSerialState(ConnectionState.STREAMERROR);
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if(heartbeat-- == 0){
                        heartbeat = HEARTBEAT;
                        Log.d(TAG,"pulse SerialReceive");
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionState.IDLE);
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
                        getSerialData().setSerialName("Connection timed out");
                        setSerialState(ConnectionState.STREAMERROR);
                        serialService.stopSelf();
                    }
                    Thread.sleep(DELAY);
                    if (heartbeat-- == 0) {
                        heartbeat = HEARTBEAT;
                        Log.d(TAG, "pulse SerialSend");
                    }
                } catch (InterruptedException e) {
                    setSerialState(ConnectionState.IDLE);
                    Log.d(TAG, "SerialSendThread stopped" + e.toString());
                    serialService.stopSelf();
                }
            }
        }
    }//SerialSendThread
}
