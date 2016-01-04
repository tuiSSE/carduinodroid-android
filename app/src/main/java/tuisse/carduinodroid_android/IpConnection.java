package tuisse.carduinodroid_android;


import android.content.Intent;
import android.util.Log;

/**
 * Created by keX on 04.01.2016.
 */
public class IpConnection {
    protected IpService ipService;

    protected IpSendThread          ipSendThread;
    protected IpReceiveThread       ipReceiveThread;

    IpConnection(IpService s){
        ipService = s;
        ipSendThread = new IpSendThread();
        ipReceiveThread = new IpReceiveThread();
    }

    protected class IpReceiveThread extends Thread {
        public IpReceiveThread() {
            super("IpConnection-IpReceiveThread");
        }
        @Override
        public void run() {

        }
    }//IpReceiveThread

    protected class IpSendThread extends Thread {
        public IpSendThread() {
            super("IpConnection-IpSendThread");
        }
        @Override
        public void run() {

        }
    }//IpSendThread
}
