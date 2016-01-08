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

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    static private boolean isDestroyed = false;
    private CarduinodroidApplication carduino;
    private SerialConnection serial;
    private Handler handler;
    private PowerManager.WakeLock mWakeLock;

    private static final int NOTIFICATION = 1337;
    public static final String EXIT_ACTION = "tuisse.carduinodroid_android.EXIT";

    @Nullable
    private NotificationManager mNotificationManager = null;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

    protected CarduinodroidApplication getCarduino(){
        return carduino;
    }

    private void setupNotifications() { //called in onCreate()
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StatusActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StatusActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .setAction(EXIT_ACTION),
                0);
        mNotificationBuilder
                .setSmallIcon(R.drawable.logo_white)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getText(R.string.appName))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.exitApp), pendingCloseIntent)
                .setOngoing(true);
    }

    protected void showNotification() {
        mNotificationBuilder
                .setWhen(System.currentTimeMillis())
                .setTicker(carduino.dataContainer.serialData.getConnectionState().getStateName())
                .setContentText(carduino.dataContainer.serialData.getConnectionState().getStateName());
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION, mNotificationBuilder.build());
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
/*
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
*/
        setupNotifications();
        carduino = (CarduinodroidApplication) getApplication();

        if(carduino.dataContainer.preferences.getSerialPref().isBluetooth()){
            serial = new SerialBluetooth(this);
            Log.d(TAG, "onCreated SerialBluetooth");
        }
        else if(carduino.dataContainer.preferences.getSerialPref().isUsb()){
            serial = new SerialUsb(this);
            Log.d(TAG, "onCreated SerialUsb");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        isDestroyed = false;
        if(serial.isError()) {
            Log.i(TAG, "resetting serial");
            serial.reset();
        }
        if (!serial.isIdle()) {
            Log.d(TAG, "serial already started");
            return START_STICKY;
        }
        new Thread(new Runnable() {
            public void run() {
                if(!serial.find()){
                    stopSelf();
                }
                else if(serial.connect()) {
                    serial.start();
                }
                else{
                    stopSelf();
                }
                if(isDestroyed){
                    //if service should stop during connection, stop all threads
                    serial.close();
                }
            }
        }, "connectSerialThread").start();
        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        isDestroyed = true;
        //disconnect
        serial.close();
        mNotificationManager.cancel(NOTIFICATION);
        //mWakeLock.release();
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
