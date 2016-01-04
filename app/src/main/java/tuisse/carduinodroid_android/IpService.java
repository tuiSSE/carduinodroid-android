package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class IpService extends Service {

    static final String TAG = "CarduinoSerialService";

    private CarduinodroidApplication carduino;
    private IpConnection ipConnection;


    protected CarduinodroidApplication getCarduino(){
        return carduino;
    }

    public IpService() {
        ipConnection = new IpConnection(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        carduino = (CarduinodroidApplication) getApplication();
        Log.d(TAG, "onCreated");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        Log.i(TAG, "onDestroyed");
    }
}
