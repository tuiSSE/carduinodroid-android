package tuisse.carduinodroid_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SerialService extends Service {
    static final String TAG = "CarduinoSerialService";
    private CarduinodroidApplication carduino;
    private SerialConnection serial;
    //private PowerManager.WakeLock mWakeLock;

    private static final int NOTIFICATION = 1337;
    public static final String EXIT_ACTION = "tuisse.carduinodroid_android.EXIT";

    @Nullable
    private NotificationManager mNotificationManager = null;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);

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
                .setSmallIcon(R.drawable.logo_inv)
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
                .setTicker(serial.serialState.getStateName())
                .setContentText(serial.serialState.getStateName());
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
/*
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
*/
        setupNotifications();
        carduino = (CarduinodroidApplication) getApplication();
        serial = new SerialBluetooth(getApplication(),this);
        Log.d(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(serial.isError()) {
            Log.e(TAG, "resetting serial");
            serial.reset();
        }
        if (!serial.isIdle()) {
            Log.d(TAG, "serial already started");
            return START_STICKY;
        }
        new Thread(new Runnable() {
            public void run() {
                serial.find();
                if(serial.connect()) {
                    serial.start();
                }
                else{
                    stopSelf();
                }
            }
        }, "connectSerialThread").start();
        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        serial.close();
        mNotificationManager.cancel(NOTIFICATION);
       // mWakeLock.release();
        //disconnect
        Log.i(TAG, "onDestroyed");
    }



}
