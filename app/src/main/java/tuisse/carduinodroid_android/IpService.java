package tuisse.carduinodroid_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tuisse.carduinodroid_android.data.CarduinoData;
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
    static private boolean isRecreating = false;

    protected StartingIpConnection startingIpConnection;
    protected StoppingIpConnection stoppingIpConnection;

    protected CarduinodroidApplication getCarduino(){

        return carduino;
    }

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
            Log.e(TAG, "Fatal Error - There should be no IP Service started");
            stopSelf();
            return START_STICKY;
        }

        if (getDData().getIpState().isError()) {
            Log.e(TAG, "Error - Service has started with an Error. Try to reset");
            if (ip != null) {
                ip.close();
                ip = null;
            } else getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
        }

        if (getDData().getIpState().isFound()) {
            Log.e(TAG, "Error - Service should not be in this State. Try to reset");
            if (ip != null) {
                ip.close();
                ip = null;
            } else getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
        }

        if (!getDData().getIpState().isIdle()) {
            if (ip != null) {
                Log.e(TAG, "Service has been already started");
                return START_STICKY;
            } else {
                Log.e(TAG, "Emergency - IP Connection has no object but a state as if it runs");
                getDData().setIpState(new ConnectionState(ConnectionEnum.IDLE));
            }
        }

        if (getDData().getIpState().isIdle() && !isDestroyed && !isClosing && !isRecreating) {
            isClosing = true;
            isRecreating = true;
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
                    isClosing = false;
                    isRecreating = false;
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

        startingIpConnection.interrupt();
        isRecreating = false;

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
                //isClosing = true;
                ip.close();
                while(isClosing){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Error while Sleeping during the Starting Sequence");
                        e.printStackTrace();
                        getDData().setIpState(new ConnectionState(ConnectionEnum.ERROR));
                        break;
                    }
                }
                ip = null;
            }
            isClosing = false;
            //So first closes the old IP Connection and Rebuild it then
            if (ip == null) {
                Log.i(TAG, "Creating IP Connection");
                ip = new IpConnection(ipService);
                getDData().setMyIp(ip.getLocalIpAddress());
                switch (getDataHandler().getControlMode()) {
                    case REMOTE:
                        ip.initClient();

                        ip.startClientThread();
                        break;
                    case TRANSCEIVER:
                        ip.initServer();

                        ip.startServerThread("CtrlSocket");
                        ip.startServerThread("DataSocket");
                        break;
                    default:
                        break;
                }
                isRecreating = false;
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
                        Log.e(TAG, "Error while Sleeping during the Stopping Sequence");
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
