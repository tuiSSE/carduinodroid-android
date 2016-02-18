/**
 * @author Till Max Schwikal
 */

package tuisse.carduinodroid_android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.CommunicationStatus;
import tuisse.carduinodroid_android.data.DataHandler;

/**
 * Service which watches the communication status of the carduinodroid system.
 */
public class WatchdogService extends Service {

    private final String TAG = "CarduinoWatchdog";

    static private boolean isDestroyed = false;
    static private boolean isInForeground = false;
    private boolean watchdogRun = false;
    private CarduinodroidApplication carduino;
    private WatchdogThread watchdogThread;
    private StopWatchdogThread stopWatchdogThread;
    private Handler handler;

    private SerialStatusChangeReceiver serialStatusChangeReceiver;
    private IntentFilter serialStatusChangeFilter;
    private IpStatusChangeReceiver ipStatusChangeReceiver;
    private IntentFilter ipStatusChangeFilter;

    @Nullable
    private NotificationManager notificationManager = null;


    private final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

    /**
     * Helper function to get the CarduinoData
     *
     * @return CarduinoData Object
     */
    private CarduinoData getData(){
        return carduino.dataHandler.getData();
    }

    /**
     * Helper function to get the CarduinoDroidData
     *
     * @return CarduinoDroidData Object
     */
    private CarduinoDroidData getDData(){
        return carduino.dataHandler.getDData();
    }

    /**
     * Helper function to get the DataHandler
     *
     * @return DataHandler Object
     */
    private DataHandler getDataHandler(){
        return carduino.dataHandler;
    }

    /**
     * static return function for the isDestroyed status.
     *
     * @return isDestroyed
     */
    static synchronized boolean getIsDestroyed(){
        return isDestroyed;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        carduino = (CarduinodroidApplication) getApplication();
        handler = new Handler();
        watchdogThread = new WatchdogThread();
        stopWatchdogThread = new StopWatchdogThread();
        setupNotifications();
        serialStatusChangeReceiver =  new SerialStatusChangeReceiver();
        serialStatusChangeFilter =    new IntentFilter(Constants.EVENT.SERIAL_STATUS_CHANGED);
        ipStatusChangeReceiver =  new IpStatusChangeReceiver();
        ipStatusChangeFilter =    new IntentFilter(Constants.EVENT.IP_DATA_RECEIVED);
        Log.i(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(stopWatchdogThread.isAlive()){
            stopWatchdogThread.interrupt();
        }
        if(!isDestroyed){
            if(watchdogThread.isAlive()){
                if(intent.getAction() != null) {
                    if (intent.getAction().equals(Constants.ACTION.CONTROL_MODE_CHANGED)) {
                        Log.d(TAG, "CONTROL_MODE_CHANGED");
                        checkCommunicationSystem();
                        return START_STICKY;
                    }
                }
                Log.d(TAG,"already started");
                return START_STICKY;
            }
            Log.d(TAG,"already started but watchdogThread is not alive");
        }
        isDestroyed = false;
        LocalBroadcastManager.getInstance(this).registerReceiver(serialStatusChangeReceiver, serialStatusChangeFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(ipStatusChangeReceiver, ipStatusChangeFilter);
        updateNotification();
        synchronized (this) {
            if(!isInForeground) {
                startForeground(Constants.NOTIFICATION_ID.WATCHDOG, notificationBuilder.build());
                isInForeground = true;
            }
        }
        if(stopWatchdogThread.isAlive()){
            stopWatchdogThread.interrupt();
        }
        watchdogThread.start();
        watchdogRun = true;
        sendToast("Watchdog Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        watchdogRun = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serialStatusChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ipStatusChangeReceiver);
        synchronized (this) {
            if(isInForeground) {
                stopForeground(false);
                isInForeground = false;
            }
            if(notificationManager != null) {
                notificationManager.cancel(Constants.NOTIFICATION_ID.WATCHDOG);
            }
        }
        if(watchdogThread.isAlive()) {
            watchdogThread.interrupt();
        }
        stopWatchdogThread.run();
        Log.i(TAG, "onDestroyed");
    }

    private void setupNotifications() { //called in onCreate()
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StatusActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, StatusActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .setAction(Constants.ACTION.EXIT),
                0);
        notificationBuilder
                .setSmallIcon(R.drawable.notification_logo_white)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getText(R.string.appName))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.exitApp), pendingCloseIntent)
                .setOngoing(true);
    }

    protected void updateNotification() {
        notificationBuilder
                .setWhen(System.currentTimeMillis())
                .setTicker(getData().getSerialState().getStateName())
                .setContentText(getData().getSerialState().getStateName());
        if (notificationManager != null) {
            notificationManager.notify(Constants.NOTIFICATION_ID.WATCHDOG, notificationBuilder.build());
        }
    }

    private class WatchdogThread extends Thread {
        public WatchdogThread() {
            super("WatchdogService-WatchdogThread");
        }
        @Override
        public void run() {
            int heartbeat = Constants.HEART_BEAT.WATCHDOG;
            Log.d(TAG, "WatchdogThread started");
            while (watchdogRun) {
                try {
                    if(heartbeat > 0) {
                        if (heartbeat-- == 0) {
                            heartbeat = Constants.HEART_BEAT.WATCHDOG;
                            Log.d(TAG, "pulse Watchdog");
                        }
                    }
                    checkCommunicationSystem();
                    if(isDestroyed){
                        stopSelf();
                    }
                    Thread.sleep(Constants.DELAY.WATCHDOG);
                } catch (InterruptedException e) {
                    Log.d(TAG, "WatchdogThread stopped: " + e.toString());
                    stopSelf();
                }
            }
            stopSelf();
        }
    }//WatchdogThread

    /**
     * Function which checks the carduinodroid communication system.
     *
     * It starts or stopps the communication services properly. It is called in the WatchdogService
     * main cycle and on an CONTROL_MODE_CHANGED intent which is sent in the WatchdogService onStartCommand.
     */
    private void checkCommunicationSystem(){
        switch (getDataHandler().getControlMode()){
            case REMOTE:
                if(getDData().getIpState().isIdleError()){
                    startService(new Intent(WatchdogService.this,IpService.class));
                }
                if(!SerialService.getIsDestroyed()){
                    stopService(new Intent(WatchdogService.this,SerialService.class));
                }
                if(getData().getSerialState().isUnknown()){
                    //request for serial State status
                    //TODO implement
                }
                break;
            case TRANSCEIVER:
                if(getData().getSerialState().isIdleError()){
                    startService(new Intent(WatchdogService.this,SerialService.class));
                }
                if(getDData().getIpState().isIdleError()){
                    startService(new Intent(WatchdogService.this,IpService.class));
                }
                break;
            default://DIRECT
                if(getData().getSerialState().isIdleError()){
                    startService(new Intent(WatchdogService.this,SerialService.class));
                }
                if(!IpService.getIsDestroyed()){
                    stopService(new Intent(WatchdogService.this,IpService.class));
                }
                break;
        }
        //renew communication status
        renewCommunicationStatus();
    }

    /**
     * stops the watchdog threads and communication services.
     *
     * The Thread stops all other services which were started.
     * This may take some time.
     */
    private class StopWatchdogThread extends Thread {
        public StopWatchdogThread() {
            super("WatchdogService-StopWatchdogThread");
        }
        @Override
        public void run() {
            if (!SerialService.getIsDestroyed()) {
                stopService(new Intent(WatchdogService.this, SerialService.class));
                if (!IpService.getIsDestroyed()) {
                    stopService(new Intent(WatchdogService.this, IpService.class));
                }
            }
            if (SerialService.getIsDestroyed() && IpService.getIsDestroyed()) {
                stopSelf();
                sendToast("WatchdogThred stopped");
                isDestroyed = true;
                return;
            }
        }
    };

    /**
     * renews the communication status.
     *
     * The function checks if the communication status has changed. And calls the proper functions
     * in DataHandler.
     *
     * @return true if overall communication status changed, otherwise false
     */
    private boolean renewCommunicationStatus(){
        CommunicationStatus csOld = getDataHandler().getCommunicationStatus();
        getDataHandler().calcCommunicationStatus();
        if(!csOld.equals(getDataHandler().getCommunicationStatus())){
            Intent onSerialConnectionStatusChangeIntent = new Intent(Constants.EVENT.COMMUNICATION_STATUS_CHANGED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(onSerialConnectionStatusChangeIntent);
            return true;
        }
        return false;
    }


    /**
     * Serial status change receiver.
     *
     * If a serial status change intent is triggered, the communication status and
     * notification is updated.
     *
     * @see WatchdogService
     */
    private class SerialStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onSerialStatusChangeReceiverReceive");
            if(renewCommunicationStatus()){
                updateNotification();
            }
        }
    }

    /**
     * Ip status change receiver.
     *
     * If a ip status change intent is triggerd, the communication status and
     * notification is updated.
     *
     * @see WatchdogService
     */
    private class IpStatusChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onIpStatusChangeReceiverReceive");
            if(renewCommunicationStatus()){
                updateNotification();
            }
        }
    }

    /**
     * Helper function to send a Toast message
     *
     * @param message: String message to be toasted
     */
    private void sendToast(final String message){
        handler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(WatchdogService.this, message, Toast.LENGTH_LONG);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    /**
     * Helper function to send a Toast message
     *
     * @param messageId: resource integer to a string that should get toasted
     */
    private void sendToast(final int messageId){
        handler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(WatchdogService.this, getString(messageId), Toast.LENGTH_LONG);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}