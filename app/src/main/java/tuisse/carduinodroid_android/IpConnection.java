package tuisse.carduinodroid_android;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
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
    //protected IpDataReceiveThread    ipDataReceiveThread;
    //protected IpDataSendThread       ipDataSendThread;
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

    //BufferedWriter outData;
    //BufferedReader inData;

    private String incomingDataMsg;

    IpConnection(IpService s){

        ipService = s;

        ipDataConnectionThread = new IpDataConnectionThread();
        //ipDataReceiveThread = new IpDataReceiveThread();
        //ipDataSendThread = new IpDataSendThread();
        ipCtrlSendThread = new IpCtrlSendThread();

        reset();

        getDData().setIpType(IpType.WLAN);
    }

    protected void reset(){

        ctrlSocket = null;
        dataSocket = null;
        ctrlClient = null;
        dataClient = null;

        //outData = null;
        //inData = null;
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
        new Thread(new Runnable() {
            public void run() {
                try {
                    if(!ctrlSocket.isClosed()){
                        Socket socketFakeCtrl = new Socket("localhost", CTRLPORT);
                        socketFakeCtrl.close();}
                    if(!dataSocket.isClosed()){
                        Socket socketFakeData = new Socket("localhost", DATAPORT);
                        socketFakeData.close();}

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
                    e.printStackTrace();
                }
            }
        }, "StopIpConnection").start();

        Log.d(TAG, "Closing IP Connection Service");
    }

    protected boolean connect(){

        if(checkServerStatus()) return connectToServer();
        else return false;
    }

    private boolean checkServerStatus(){

        return true;
    }

    private boolean connectToServer(){

        return true;
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

    protected void sendData(BufferedWriter outData) throws IOException{
        //Hier kommt noch die Art und Weise Daten zu erstellen/erfragen aus den Handlern
        Log.d(TAG, "verbunden");
        outData.write("verbunden");
        outData.newLine();
        outData.flush();
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

            while(isRunning()||isTryConnect()||isConnected()){

                dataClient = null;

                try {
                    Log.d(TAG, "Waiting for Data Connection Accept");
                    dataClient = dataSocket.accept();

                } catch (IOException e) {
                    setIpState(ConnectionEnum.UNKNOWN);
                    e.printStackTrace();
                }

                if(dataClient!=null) {
                    if (!String.valueOf(dataClient.getInetAddress().getHostName()).equals("localhost")){
                        setIpState(ConnectionEnum.CONNECTED);
                        new IpDataSendThread(dataClient).start();
                        new IpDataReceiveThread(dataClient).start();
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

        Socket socket;
        BufferedReader inData;

        public IpDataReceiveThread(Socket dataSocket) {
            super("IpConnection-IpDataReceiveThread");
            this.socket = dataSocket;
        }
        @Override
        public void run() {

            try {
                inData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                Log.d(TAG, "BufferedReader Initialization Error");
                setIpState(ConnectionEnum.ERROR);
                e.printStackTrace();
            }

            while(isConnected())
            {
                try {
                    while((incomingDataMsg = inData.readLine())!=null){
                        receiveData(incomingDataMsg);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.ERROR);
                    e.printStackTrace();
                }
                setIpState(ConnectionEnum.RUNNING);
            }
        }
    }//IpDataReceiveThread

    protected class IpDataSendThread extends Thread {

        Socket socket;
        BufferedWriter outData;

        public IpDataSendThread(Socket dataSocket) {
            //super("IpConnection-IpDataSendThread");
            this.socket = dataSocket;
            outData = null;
        }
        @Override
        public void run() {

            try {
                outData = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {

                e.printStackTrace();
            }

            while(isConnected())
            {
                try {
                    sendData(outData);
                    Thread.sleep(3000);
                } catch (IOException e) {
                    //This Error will be created be Closing Connection while sleeping
                    Log.d(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.RUNNING);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    setIpState(ConnectionEnum.ERROR);
                    e.printStackTrace();
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

                if(ctrlClient!=null) {
                    if (!String.valueOf(ctrlClient.getInetAddress().getHostName()).equals("localhost"))
                        new CtrlMsgEchoThread(ctrlClient).start();
                }
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
                if(!socket.isClosed()){
                    BufferedWriter dataInfoOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    sendDataStatus(dataInfoOut);
                    Thread.sleep(10);

                    dataInfoOut.close();
                    socket.close();
                }
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

                dataSocket = new ServerSocket();
                dataSocket.setReuseAddress(true);
                dataSocket.bind(new InetSocketAddress(DATAPORT));
                Log.d(TAG, "Created Data Server Socket");
            }else{Log.d(TAG, "Data Server Socket already initialized");}

        }else if(socketType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            if(ctrlSocket==null) {

                ctrlSocket = new ServerSocket();
                ctrlSocket.setReuseAddress(true);
                ctrlSocket.bind(new InetSocketAddress(CTRLPORT));
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
    protected boolean isConnected() { return getDData().getIpState().isConnected();}
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
