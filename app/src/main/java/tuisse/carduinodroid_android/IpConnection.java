package tuisse.carduinodroid_android;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.ConnectionState;
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

    ServerSocket ctrlSocket;
    ServerSocket dataSocket;

    Socket ctrlClient;
    Socket dataClient;

    //BufferedWriter outCtrl;
    BufferedWriter outData;
    BufferedReader inData;

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

        //outCtrl = null;
        outData = null;
        inData = null;
    }

    //Initialization of both ServerSockets if a connection via another mobile phone or pc is wished
    protected void init(){

        try {
            createSocketServer(TAG_CTRLPORT);
            createSocketServer(TAG_DATAPORT);

            setIpState(ConnectionEnum.IDLE);
        } catch (IOException e) {
            Log.d(TAG, "Problem with Setup of DATA or CONTROL Server Socket");
            setIpState(ConnectionEnum.ERROR);
            e.printStackTrace();
        }
    }

    protected void startThread(String dataType){

        if(dataType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            setIpState(ConnectionEnum.RUNNING);
            if(!ipCtrlSendThread.isAlive()) ipCtrlSendThread.start();

        }else if(dataType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            setIpState(ConnectionEnum.RUNNING);
            if(!ipDataConnectionThread.isAlive()) ipDataConnectionThread.start();
        }
        else {
            setIpState(ConnectionEnum.ERROR);
            Log.d(TAG, "Data/Ctrl Socket nicht da");
        }
    }

    protected void close(){

        setIpState(ConnectionEnum.UNKNOWN);
        try {
            if(ipDataReceiveThread.isAlive()) ipDataReceiveThread.interrupt();
            if(ipDataSendThread.isAlive()) ipDataSendThread.interrupt();
            if(ipCtrlSendThread.isAlive()) ipCtrlSendThread.interrupt();
            if(ipDataConnectionThread.isAlive()) ipDataConnectionThread.interrupt();

            if(outData!=null){
                outData.close();
            }
            if(inData!=null){
                inData.close();
            }
            if(ctrlClient!=null){
                ctrlClient.close();
            }
            if(dataClient!=null) {
                dataClient.close();
            }
            if(ctrlSocket!=null){
                ctrlSocket.close();
            }
            if(dataSocket!=null){
                dataSocket.close();
            }

        } catch (IOException e) {
            setIpState(ConnectionEnum.ERROR);
            e.printStackTrace();
        }
        setIpState(ConnectionEnum.UNKNOWN);
        Log.d(TAG, "Stop Server Thread");
    }


    // Giving information over the Control Channel if the Data Channel is free
    protected boolean requestDataStatus(){

        return (isConnected()||isTryConnect());
    }

    protected void sendDataStatus(BufferedWriter outCtrl) throws IOException{
        //Anpassen dass der Status über JSON vom Data SocketServer gesendet wird mit richtigen Zustand
        String Test = String.valueOf(requestDataStatus());
        outCtrl.write(Test);
        outCtrl.newLine();
        outCtrl.flush();
    }

    protected void sendData() throws IOException{
        //Hier kommt noch die Art und Weise Daten zu erstellen/erfragen aus den Handlern
    }

    protected void receiveData(String dataPacket) throws IOException{
        // Hier kommt noch die Verwertung und Weitergabe von JSON Paketen
        Log.d(TAG, dataPacket);
    }

    protected class IpDataConnectionThread extends Thread {
        public IpDataConnectionThread() {
            super("IpConnection-IpDataConnectionThread");
        }

        @Override
        public void run() {

            if(!ipDataReceiveThread.isAlive()) ipDataReceiveThread.start();
            if(!ipDataSendThread.isAlive()) ipDataSendThread.start();

            while(isRunning()||isTryConnect()||isConnected()){ /*!isStoppedData()*/

                dataClient = null;
                outData = null;
                inData = null;

                try {
                    Log.d(TAG, "Waiting for Data Connection Accept");
                    dataClient = dataSocket.accept();

                } catch (IOException e) {
                    setIpState(ConnectionEnum.UNKNOWN);
                    e.printStackTrace();
                }

                if(dataClient!=null){
                    try {
                        inData = new BufferedReader(new InputStreamReader(dataClient.getInputStream()));
                        outData = new BufferedWriter(new OutputStreamWriter(dataClient.getOutputStream()));
                        setIpState(ConnectionEnum.CONNECTED);
                    } catch (IOException e) {
                        setIpState(ConnectionEnum.ERROR);
                        e.printStackTrace();
                    }
                }

                while (isConnected()) {
                    //Wait until the Connection is lost and shown by the Client Receive
                    //Talk about a better solution - or just together with Receive Thread?
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        setIpState(ConnectionEnum.ERROR);
                        e.printStackTrace();
                    }
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

            while(isConnected()||isRunning()||isTryConnect())
            {
                if(isConnected()){
                    try {
                        while((incomingDataMsg = inData.readLine())!=null){
                            receiveData(incomingDataMsg);
                        }
                    } catch (IOException e) {
                        setIpState(ConnectionEnum.ERROR);
                        e.printStackTrace();
                    }
                    setIpState(ConnectionEnum.RUNNING);
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

            while(isConnected()||isRunning()||isTryConnect())
            {
                if(isConnected()){
                    try {
                        sendData();
                    } catch (IOException e) {
                        setIpState(ConnectionEnum.STREAMERROR);
                        e.printStackTrace();
                    }
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

            // Stay Rdy for Request of Transmission over Control Server Socket
            while (isRunning()||isTryConnect()||isConnected()) {
                ctrlClient = null;

                try {
                    Log.d(TAG, "Waiting for Ctrl Connection Accept");
                    ctrlClient = ctrlSocket.accept();
                    // new thread for each client request
                } catch (IOException e) {
                    setIpState(ConnectionEnum.UNKNOWN);
                    e.printStackTrace();
                }
                if(ctrlClient!=null)
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

                BufferedWriter dataInfoOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                sendDataStatus(dataInfoOut);
                Thread.sleep(10);

                dataInfoOut.close();
                socket.close();

            } catch (IOException e) {
                setIpState(ConnectionEnum.TRYCONNECTERROR);
                e.printStackTrace();
            } catch (InterruptedException e) {
                setIpState(ConnectionEnum.ERROR);
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

    protected boolean isIdle() {
        return getDData().getIpState().isIdle();
    }
    protected boolean isConnected() {
        return getDData().getIpState().isConnected();
    }
    protected boolean isError() {
        return getDData().getIpState().isError();
    }
    protected boolean isTryConnect() { return getDData().getIpState().isTryConnect(); }
    protected boolean isRunning() {
        return getDData().getIpState().isRunning();
    }
    protected boolean isUnknown() {
        return getDData().getIpState().isUnknown();
    }

    protected synchronized void setIpState(ConnectionEnum state){
        setIpState(state, "");
    }

    protected synchronized void setIpState(ConnectionEnum state, String error){

        Log.d(TAG, "Changing State");
        if(getDData().getIpState() != null) {
            getDData().setIpState(new ConnectionState(state, error));
            Log.d(TAG, "IP State changed: " + getDData().getIpState().getStateName());
        }else{
            Log.e(TAG,"no getIpData()");
        }
    }

    protected synchronized CarduinoDroidData getDData(){
        return ipService.getCarduino().dataHandler.getDData();
    }

    protected synchronized CarduinoData getData(){
        return ipService.getCarduino().dataHandler.getData();
    }
}
