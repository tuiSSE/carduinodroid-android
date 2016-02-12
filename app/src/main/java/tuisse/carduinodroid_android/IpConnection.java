package tuisse.carduinodroid_android;


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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

    protected IpDataConnectionServerThread ipDataConnectionServerThread;
    protected IpCtrlSendServerThread       ipCtrlSendServerThread;

    static final String TAG = "CarduinoIpConnection";

    private static final int DATAPORT = 12020;
    private static final int CTRLPORT = 12021;
    private static final String TAG_DATAPORT = "DataSocket";
    private static final String TAG_CTRLPORT = "CtrlSocket";

    ServerSocket ctrlSocketServer;
    ServerSocket dataSocketServer;

    Socket ctrlSocket;
    Socket dataSocket;
    Socket remoteCtrlSocket;
    Socket remoteDataSocket;

    private String incomingDataMsg;

    IpConnection(IpService s){

        ipService = s;

        ipDataConnectionServerThread = new IpDataConnectionServerThread();
        ipCtrlSendServerThread = new IpCtrlSendServerThread();

        reset();

        getDData().setIpType(IpType.WLAN);
    }

    protected void reset(){

        ctrlSocketServer = null;
        dataSocketServer = null;
        ctrlSocket = null;
        dataSocket = null;
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

    //Creates a SocketServer depending on the expected Type (Data,Ctrl)
    protected boolean createSocketServer(String socketType) throws IOException {

        Log.d(TAG, "Create Server Socket");
        if(socketType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            if(dataSocketServer==null){

                dataSocketServer = new ServerSocket();
                dataSocketServer.setReuseAddress(true);
                dataSocketServer.bind(new InetSocketAddress(DATAPORT));
                Log.d(TAG, "Created Data Server Socket");
            }else{Log.d(TAG, "Data Server Socket already initialized");}

        }else if(socketType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            if(ctrlSocketServer==null) {

                ctrlSocketServer = new ServerSocket();
                ctrlSocketServer.setReuseAddress(true);
                ctrlSocketServer.bind(new InetSocketAddress(CTRLPORT));
                Log.d(TAG, "Created Ctrl Server Socket");
            }else{Log.d(TAG, "Ctrl Server Socket already initialized");}

        }else{
            Log.d(TAG, "Server Socket has the wrong type");
            return false;
        }

        return true;
    }

    protected void startThread(String dataType){

        if(dataType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            setIpState(ConnectionEnum.RUNNING);
            if(!ipCtrlSendServerThread.isAlive()) ipCtrlSendServerThread.start();

        }else if(dataType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            setIpState(ConnectionEnum.RUNNING);
            if(!ipDataConnectionServerThread.isAlive()) ipDataConnectionServerThread.start();
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
                    //hard work around for client.accept to cancel them without exception and do not miss used expetion
                    if(!ctrlSocketServer.isClosed()){
                        Socket socketFakeCtrl = new Socket("localhost", CTRLPORT);
                        socketFakeCtrl.close();}
                    if(!dataSocketServer.isClosed()){
                        Socket socketFakeData = new Socket("localhost", DATAPORT);
                        socketFakeData.close();}

                    if(ctrlSocket!=null){
                        ctrlSocket.close();
                    }
                    if(dataSocket!=null) {
                        dataSocket.close();
                    }
                    if(ctrlSocketServer!=null){
                        ctrlSocketServer.close();
                    }
                    if(dataSocketServer!=null){
                        dataSocketServer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "StopIpConnection").start();

        Log.d(TAG, "Closing IP Connection Service");
    }

    protected void connectClient(String address){

        new ClientConnection(address).start();
    }

    private boolean getServerStatus(String address){

        return true;
    }

    private boolean connectToServer(String address){

        return true;
    }

    protected boolean disconnectFromServer(){

        return true;
    }

    // Giving information over the Control Channel if the Data Channel is free
    protected boolean requestDataStatus(){

        return (isConnected()||isTryConnect());
    }

    protected void sendDataServerStatus(BufferedWriter outCtrl) throws IOException{
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

    protected class IpDataConnectionServerThread extends Thread {
        public IpDataConnectionServerThread() {super("IpConnection-IpDataConnectionServerThread");}

        @Override
        public void run() {

            while(isRunning()||isTryConnect()||isConnected()){

                dataSocket = null;

                try {
                    Log.d(TAG, "Waiting for Data Connection Accept");
                    dataSocket = dataSocketServer.accept();

                } catch (IOException e) {
                    setIpState(ConnectionEnum.UNKNOWN);
                    e.printStackTrace();
                }

                if(dataSocket!=null) {
                    if (!String.valueOf(dataSocket.getInetAddress().getHostName()).equals("localhost")){
                        setIpState(ConnectionEnum.CONNECTED);
                        new IpDataSendThread(dataSocket).start();
                        new IpDataReceiveThread(dataSocket).start();
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
                Log.d(TAG, "OutpuStream has an Error");
                setIpState(ConnectionEnum.ERROR);
                e.printStackTrace();
            }

            while(isConnected())
            {
                try {
                    sendData(outData);
                    //Real time trigger to set up with Max
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
    protected class IpCtrlSendServerThread extends Thread {
        public IpCtrlSendServerThread() {
            super("IpConnection-IpCtrlSendServerThread");
        }
        @Override
        public void run() {

            // Stay Rdy for Request of Transmission over Control Server Socket
            while (isRunning()||isTryConnect()||isConnected()) {
                ctrlSocket = null;

                try {
                    Log.d(TAG, "Waiting for Ctrl Connection Accept");
                    ctrlSocket = ctrlSocketServer.accept();
                    // new thread for each client request
                } catch (IOException e) {
                    setIpState(ConnectionEnum.UNKNOWN);
                    e.printStackTrace();
                }

                if(ctrlSocket!=null) {
                    if (!String.valueOf(ctrlSocket.getInetAddress().getHostName()).equals("localhost"))
                        new CtrlMsgEchoThread(ctrlSocket).start();
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
                    sendDataServerStatus(dataInfoOut);
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

    public class ClientConnection extends Thread{

        BufferedWriter outData;
        protected String address;

        public ClientConnection(String ipAddress){
            this.address = ipAddress;
        }

        public void run(){
            try {
                //remoteCtrlSocket = new Socket(address, CTRLPORT);
                remoteCtrlSocket = new Socket("192.168.178.24", 12022);
                setIpState(ConnectionEnum.CONNECTED);
                //new IpDataReceiveThread(checkDataSocket).start();

            } catch (IOException e) {
                Log.d(TAG, "Client cant connect to Ctrl Server Socket");
                e.printStackTrace();
            }
        }
    }

    protected boolean isIdle() { return getDData().getIpState().isIdle();}
    protected boolean isConnected() { return getDData().getIpState().isConnected();}
    protected boolean isError() { return getDData().getIpState().isError();}
    protected boolean isTryConnect() { return getDData().getIpState().isTryConnect(); }
    protected boolean isRunning() { return getDData().getIpState().isRunning();}
    protected boolean isUnknown() { return getDData().getIpState().isUnknown();}

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
