package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WatchdogService extends Service {
    private final String TAG = "CarduinoWatchdog";
    private boolean isDestroyed = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "onCreated");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        isDestroyed = false;

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        //disconnect

        //mWakeLock.release();
        Log.i(TAG, "onDestroyed");
    }
}
