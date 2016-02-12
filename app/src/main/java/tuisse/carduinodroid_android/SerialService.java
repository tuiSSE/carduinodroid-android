package tuisse.carduinodroid_android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataHandler;

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    static private boolean isDestroyed = true;
    private CarduinodroidApplication carduino;
    private SerialConnection serial = null;
    private Handler handler;

    private CarduinoData getData(){
        return carduino.dataHandler.getData();
    }
    private DataHandler getDataHandler(){
        return carduino.dataHandler;
    }

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
        handler = new Handler();
        carduino = (CarduinodroidApplication) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isDestroyed = false;
        if(getData().getSerialState().isUnknown()){
            Log.e(TAG,"FATAL: this device should not start serial service!");
            serial = null;
            stopSelf();
            return START_STICKY;
        }
        if(getData().getSerialState().isError()) {
            Log.i(TAG, "resetting serial");
            if(serial != null){
                serial.reset();
            }
            else{
                getData().setSerialState(new ConnectionState(ConnectionEnum.IDLE, ""));
            }
        }
        if (!getData().getSerialState().isIdle()) {
            Log.d(TAG, "serial already started");
            return START_STICKY;
        }
        if(getDataHandler().getSerialPref().isBluetooth()){
            serial = new SerialBluetooth(this);
                Log.d(TAG, "onCreated SerialBluetooth");
        }
        else if(getDataHandler().getSerialPref().isUsb()){
                serial = new SerialUsb(this);
                Log.d(TAG, "onCreated SerialUsb");
        }
        else{
            Log.e(TAG, "FATAL on start: serial permission is not either bluetooth or usb");
            sendToast("FATAL on start: serial permission is not either bluetooth or usb");
            stopSelf();
            return START_STICKY;
        }

        if(serial != null){
            new Thread(new Runnable() {
                public void run() {
                    if(!serial.find()){
                        stopSelf();
                    }
                    else if(!serial.connect()) {
                        stopSelf();
                    }
                    else{
                        serial.start();
                    }
                    if(isDestroyed){
                        //if service should stop during connection, stop all threads
                        serial.close();
                    }
                }
            }, "connectSerialThread").start();
            return START_STICKY;
        }
        else{
            Log.e(TAG, "FATAL on start: serial is not created");
            sendToast("FATAL on start: serial is not created");
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //disconnect
        if(serial != null){
            serial.close();
        } else {
            Log.e(TAG, "FATAL on close: serial was not created");
        }
        isDestroyed = true;
        Log.i(TAG, "onDestroyed");
    }

    protected void sendToast(final String message){
        handler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(SerialService.this, message, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    protected void sendToast(final int messageId){
        handler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(SerialService.this, getString(messageId), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
