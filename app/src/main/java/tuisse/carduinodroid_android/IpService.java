package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
import tuisse.carduinodroid_android.data.DataHandler;

public class IpService extends Service {

    static final String TAG = "CarduinoIpService";
    private CarduinodroidApplication carduino;
    private IpConnection ip;

    static private boolean isDestroyed = true;
    static private boolean isClosing = false;
    protected StartingIpConnection startingIpConnection;
    protected StoppingIpConnection stoppingIpConnection;

    private DataHandler getDataHandler(){
        return carduino.dataHandler;
    }
    private CarduinoDroidData getDData() {
        return carduino.dataHandler.getDData();
    }

    protected CarduinodroidApplication getCarduino() {
        return carduino;
    }

    static public boolean getIsDestroyed() {
        return isDestroyed;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        carduino = (CarduinodroidApplication) getApplication();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Starting the Service more then one time before closing can cause still some errors
        isDestroyed = false;

        if (getDData().getIpState().isUnknown()) {
            Log.i(TAG, "Fatal Error - There should be no IP Service started");
            stopSelf();
            return START_STICKY;
        }

        if (getDData().getIpState().isError()) {
            Log.i(TAG, "Error - Service has started with an Error. Try to reset");
            if (ip != null) {
                ip.close();
                ip = null;
            } else getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
        }

        if (getDData().getIpState().isFound()) {
            Log.i(TAG, "Error - Service should no be in this State. Try to reset");
            if (ip != null) {
                ip.close();
                ip = null;
            } else getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
        }

        if (!getDData().getIpState().isIdle()) {
            if (ip != null) {
                Log.i(TAG, "Service has been already started");
                return START_STICKY;
            } else {
                Log.i(TAG, "Emergency - IP Connection has no object but a state as if it runs");
                getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
            }
        }

        if (getDData().getIpState().isIdle() && !isDestroyed && !isClosing) {

            switch (getDataHandler().getControlMode()) {
                case REMOTE:
                    startingIpConnection = new StartingIpConnection(this);
                    startingIpConnection.start();
                    break;
                case TRANSCEIVER:
                    startingIpConnection = new StartingIpConnection(this);
                    startingIpConnection.start();
                    break;
                default:
                    stopSelf();
                    break;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        isClosing = true;

        startingIpConnection.interrupt();

        stoppingIpConnection = new StoppingIpConnection();
        stoppingIpConnection.start();

        Log.i(TAG, "onDestroyed");
    }

    public class StartingIpConnection extends Thread {

        protected IpService ipService;

        public StartingIpConnection(IpService Service) {
            this.ipService = Service;}

        public void run() {
                //Secures a full rebuild especially for the important Threads for sending/receiving
                if (ip != null) {
                    isClosing = true;
                    ip.close();
                    while(isClosing){
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            Log.i(TAG, "Error while Sleeping during the Starting Sequence");
                            e.printStackTrace();
                            getDData().setIpState(new ConnectionState(ConnectionEnum.ERROR));
                            break;
                        }
                    }
                    ip = null;
                }
                //So first closes the old IP Connection and Rebuild it then
                if (ip == null) {
                    Log.i(TAG, "Creating IP Connection");
                    ip = new IpConnection(ipService);

                    switch (getDataHandler().getControlMode()) {
                        case REMOTE:
                            ip.initClient();
                            ip.connectClient("192.168.178.31");
                            break;
                        case TRANSCEIVER:
                            ip.initServer();

                            ip.startThread("CtrlSocket");
                            ip.startThread("DataSocket");
                            break;
                        default:
                            break;
                    }

                }
        }
    }

    public class StoppingIpConnection extends Thread {

        public StoppingIpConnection() {}

        public void run() {

            if (ip != null) {
                ip.close();
                while (isClosing) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Error while Sleeping during the Stopping Sequence");
                        e.printStackTrace();
                        getDData().setIpState(new ConnectionState(ConnectionEnum.ERROR));
                        break;
                    }
                }
                ip = null;
            }
        }
    }

    protected void setIsClosing(Boolean IsClosing){
        this.isClosing = IsClosing;
    }
}
