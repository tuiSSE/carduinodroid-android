package tuisse.carduinodroid_android;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.IpType;

/**
 * Created by keX on 04.01.2016.
 */
public class IpConnection {
    protected IpService ipService;

    protected IpDataConnectionThread ipDataConnectionThread;
    protected IpDataReceiveThread    ipDataReceiveThread;
    protected IpDataSendThread       ipDataSendThread;
    protected IpCtrlSendThread       ipCtrlSendThread;

    static final String TAG = "CarduinoIpConnection";

    private static final int DATAPORT = 12020;
    private static final int CTRLPORT = 12021;
    private static final String TAG_DATAPORT = "DataSocket";
    private static final String TAG_CTRLPORT = "CtrlSocket";

    private boolean ctrlStatusSend;

    ServerSocket ctrlSocket;
    ServerSocket dataSocket;

    Socket ctrlClient;
    Socket dataClient;

    BufferedWriter outCtrl;
    BufferedWriter outData;
    BufferedReader inData;

    private String outgoingCtrlMsg;
    private String incomingDataMsg;
    private String outgoingDataMsg;

    IpConnection(IpService s){

        ipService = s;

        ipDataConnectionThread = new IpDataConnectionThread();
        ipDataReceiveThread = new IpDataReceiveThread();
        ipDataSendThread = new IpDataSendThread();
        ipCtrlSendThread = new IpCtrlSendThread();

        reset();

        getDData().setIpType(IpType.WLAN);
    }

    protected void reset(){

        ctrlSocket = null;
        dataSocket = null;
        ctrlClient = null;
        dataClient = null;

        outCtrl = null;
        outData = null;
        inData = null;

        ctrlStatusSend = false;
    }

    //Initialization of both ServerSockets if a connection via another mobile phone or pc is wished
    protected boolean init(){

        try {
            createSocketServer(TAG_CTRLPORT);
            createSocketServer(TAG_DATAPORT);
        } catch (IOException e) {
            /* Hier SOCKET SERVER ERROR*/
            e.printStackTrace();
        }
        return true;
    }

    protected void startThread(String dataType){
        Log.d(TAG, "Start Server Threads and BufferedWriter/Reader");

        if(dataType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            ipCtrlSendThread.start();

        }else if(dataType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            ipDataConnectionThread.start();
        }
        else Log.d(TAG, "Data/Ctrl Socket nicht da"); /*UNKOWN ERROR State*/

    }

    protected void resetThreads(){
        Log.d(TAG, "Reset Server Thread");
        ipDataConnectionThread.interrupt();
        ipDataReceiveThread.interrupt();
        ipCtrlSendThread.interrupt();
        ipDataSendThread.interrupt();
    }

    protected void close(){

        Log.d(TAG, "Stop Server Thread");

    }

    // Giving information over the Control Channel if the Data Channel is free
    protected boolean requestDataStatus(){

        return true;
    }

    protected void sendDataStatus(BufferedWriter outCtrl) throws IOException{
        //Anpassen dass der Status über JSON vom Data SocketServer gesendet wird mit richtigen Zustand
        outCtrl.write("Test");
        outCtrl.newLine();
        outCtrl.flush();
    }

    protected void sendData() throws IOException{

    }

    protected void receiveData() throws IOException{

    }

    protected class IpDataConnectionThread extends Thread {
        public IpDataConnectionThread() {
            super("IpConnection-IpDataConnectionThread");
        }

        @Override
        public void run() {

            ipDataReceiveThread.start();
            ipDataSendThread.start();

            while(true){ /*!isStoppedData()*/
                dataClient = null;
                outData = null;
                inData = null;

                try {
                    Log.d(TAG, "warte auf data verbindung");
                    dataClient = dataSocket.accept();

                    inData = new BufferedReader(new InputStreamReader(dataClient.getInputStream()));
                    outData = new BufferedWriter(new OutputStreamWriter(dataClient.getOutputStream()));
                } catch (IOException e) {
                    /* Set DATA CONNECTION ERROR */
                    e.printStackTrace();
                }

                String c;
                try {
                    while((c = inData.readLine())!=null){

                        Log.d(TAG, c);
                    } //END while
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class IpDataReceiveThread extends Thread {
        public IpDataReceiveThread() {
            super("IpConnection-IpDataReceiveThread");
        }
        @Override
        public void run() {

            while(true) /* !isStoppedData() */
            {
                //isConnected()
                if(isConnected()){

                }
            }
        }
    }//IpDataReceiveThread

    protected class IpDataSendThread extends Thread {
        public IpDataSendThread() {
            super("IpConnection-IpDataSendThread");
        }
        @Override
        public void run() {

            while(true) /* !isStoppedData() */
            {
                //isConnected()
                if(isConnected()){

                }
            }
        }
    }//IpDataSendThread

    // Handling of the Control Message for multiple Requests
    protected class IpCtrlSendThread extends Thread {
        public IpCtrlSendThread() {
            super("IpConnection-IpCtrlSendThread");
        }
        @Override
        public void run() {

            // Stay Rdy for Request of Transmission over Control Server Socket - True zu ctrlIsConnected() ändern
            while (true) { /* !isStoppedCtrl() als Abfrage hier*/

                try {

                    ctrlClient = ctrlSocket.accept();
                } catch (IOException e) {
                    /* Hier CTRL CLIENT CONNECTION ERROR - isStopped = true*/
                    System.out.println("I/O error: " + e);
                }
                // new thread for each client request
                new CtrlMsgEchoThread(ctrlClient).start();
            }
        }
    }//IpSendThread

    public class CtrlMsgEchoThread extends Thread {
        protected Socket socket;

        public CtrlMsgEchoThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        public void run() {

            try {

                BufferedWriter dataInfoOut = new BufferedWriter(new OutputStreamWriter(ctrlClient.getOutputStream()));
                sendDataStatus(dataInfoOut);
                //sendCloseCtrlClient();

                //Test if it is necessary to close the connection (memory or other things ... programm will still run
                Thread.sleep(100);
                dataInfoOut.close();
                socket.close();
            } catch (IOException e) {
                /* Hier CLIENT SENDING ERROR - Hier Einzel Fehler somit nicht für alle vorhanden ?*/
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    //Creates a SocketServer depending on the expected Type (Data,Ctrl)
    protected boolean createSocketServer(String socketType) throws IOException {

        Log.d(TAG, "Create Server Socket");
        if(socketType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            if(dataSocket==null){

                dataSocket = new ServerSocket(DATAPORT);
                Log.d(TAG, "Created Data Server Socket");
            }else{Log.d(TAG, "Data Server Socket already initialized");}

        }else if(socketType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            if(ctrlSocket==null) {

                ctrlSocket = new ServerSocket(CTRLPORT);
                Log.d(TAG, "Created Ctrl Server Socket");
            }else{Log.d(TAG, "Ctrl Server Socket already initialized");}

        }else{
            Log.d(TAG, "Server Socket has the wrong type");
            return false;
        }

        return true;
    }

    protected boolean closeDataSocketServer(){
        Log.d(TAG, "Close Data Server Socket");
        try {
            if(dataSocket != null)
                dataSocket.close();

        } catch (IOException e) {
            Log.d(TAG, "Failure Close Data Server Socket");
            /* Hier SOCKET SERVER ERROR*/
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean closeCtrlSocketServer(){
        Log.d(TAG, "Close Ctrl Server Socket");
        try {
            if(ctrlSocket != null)
                ctrlSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "Failure Close Ctrl Server Socket");
            /* Hier SOCKET SERVER ERROR*/
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean closeSocketServer(){
        Log.d(TAG, "Close Server Sockets");
        if(closeDataSocketServer() && closeCtrlSocketServer())
            return true;
        else return false;
    }

    protected boolean isIdle() {
        return getDData().getIpState().isIdle();
    }
    protected boolean isFound() {
        return getDData().getIpState().isFound();
    }
    protected boolean isConnected() {
        return getDData().getIpState().isConnected();
    }
    protected boolean isError() {
        return getDData().getIpState().isError();
    }
    protected boolean isTryConnect() {
        return getDData().getIpState().isTryConnect();
    }
    protected boolean isRunning() {
        return getDData().getIpState().isRunning();
    }
    protected boolean isUnknown() {
        return getDData().getIpState().isUnknown();
    }

    protected synchronized CarduinoDroidData getDData(){
        return ipService.getCarduino().dataHandler.getDData();
    }

    protected synchronized CarduinoData getData(){
        return ipService.getCarduino().dataHandler.getData();
    }
}
