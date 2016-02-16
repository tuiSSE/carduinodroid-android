package tuisse.carduinodroid_android;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
import tuisse.carduinodroid_android.data.DataHandler;
import tuisse.carduinodroid_android.data.IpType;

/**
 * Created by keX on 04.01.2016.
 */
public class IpConnection {
    protected IpService ipService;

    protected IpDataConnectionServerThread ipDataConnectionServerThread;
    protected IpCtrlSendServerThread       ipCtrlSendServerThread;

    static final String TAG = "CarduinoIpConnection";

    ServerSocket ctrlSocketServer;
    ServerSocket dataSocketServer;

    Socket ctrlSocket;
    Socket dataSocket;
    Socket remoteCtrlSocket;
    Socket remoteDataSocket;

    protected boolean ctrlSocketServerDisconnected;
    protected boolean dataSocketServerDisconnected;

    private String incomingDataMsg;

    IpConnection(IpService s){

        ipService = s;

        reset();
    }

    protected void reset(){

        ctrlSocketServer = null;
        dataSocketServer = null;
        ctrlSocket = null;
        dataSocket = null;
    }

    protected DataHandler getDataHandler(){
        return ipService.getCarduino().dataHandler;
    }

    //Initialization of both ServerSockets if a connection via another mobile phone or pc is wished
    protected void initServer(){

        ipDataConnectionServerThread = new IpDataConnectionServerThread();
        ipCtrlSendServerThread = new IpCtrlSendServerThread();

        try {
            createSocketServer(Constants.IP_CONNECTION.TAG_CTRLPORT);
            createSocketServer(Constants.IP_CONNECTION.TAG_DATAPORT);
        } catch (IOException e) {
            Log.d(TAG, "Problem with Setup of DATA or CONTROL Server Socket");
            setIpState(ConnectionEnum.ERROR);
            e.printStackTrace();
        }
    }

    //Creates a SocketServer depending on the expected Type (Data,Ctrl)
    protected boolean createSocketServer(String socketType) throws IOException {

        if(socketType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_DATAPORT.toLowerCase()))
        {
            if(dataSocketServer==null){

                Log.d(TAG, "Create Data Server Socket");
                dataSocketServer = new ServerSocket();
                dataSocketServer.setReuseAddress(true);
                dataSocketServer.bind(new InetSocketAddress(Constants.IP_CONNECTION.DATAPORT));
                Log.d(TAG, "Created Data Server Socket");
            }else{Log.d(TAG, "Data Server Socket already initialized");}

        }else if(socketType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_CTRLPORT.toLowerCase()))
        {
            if(ctrlSocketServer==null) {

                Log.d(TAG, "Create Ctrl Server Socket");
                ctrlSocketServer = new ServerSocket();
                ctrlSocketServer.setReuseAddress(true);
                ctrlSocketServer.bind(new InetSocketAddress(Constants.IP_CONNECTION.CTRLPORT));
                Log.d(TAG, "Created Ctrl Server Socket");
            }else{Log.d(TAG, "Ctrl Server Socket already initialized");}

        }else{
            Log.d(TAG, "Server Socket has the wrong type");
            return false;
        }

        return true;
    }

    protected void startThread(String dataType){

        if(dataType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_CTRLPORT.toLowerCase()))
        {
            if(!ipCtrlSendServerThread.isAlive()){
                setIpState(ConnectionEnum.TRYFIND);
                ipCtrlSendServerThread.start();
            }

        }else if(dataType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_DATAPORT.toLowerCase()))
        {
            if(!ipDataConnectionServerThread.isAlive()) ipDataConnectionServerThread.start();
        }
        else {
            setIpState(ConnectionEnum.ERROR);
            Log.d(TAG, "Data/Ctrl Socket nicht da");
        }
    }

    protected void initClient(){
        setIpState(ConnectionEnum.TRYCONNECT);
        //überhaupt notwendig, da kein Socket oder ähnliches vorhanden sein muss um eine Verbindung
        //und die Threads immer als neue Instanz Realisiert werden
    }

    protected void connectClient(String address){

        new ClientConnection(address).start();
    }

    protected void close(){

        Log.d(TAG, "Closing IP Connection Service");
        //ipService.setIsClosing(true);

        new Thread(new Runnable() {

            protected int counter = 0;

            public void run() {
                try {
                    //hard work around for client.accept to cancel them without exception and do not miss used expetion

                    if(ctrlSocketServer!=null){
                        if(!ctrlSocketServer.isClosed()/* && isTryFind()*/){

                            Socket socketFakeCtrl = new Socket("localhost", Constants.IP_CONNECTION.CTRLPORT);
                            socketFakeCtrl.setSoTimeout(500);
                            socketFakeCtrl.close();
                        }

                        while(!ctrlSocketServerDisconnected){
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                Log.i(TAG, "Error while Sleeping during the Stopping Sequence");
                                e.printStackTrace();
                                setIpState(ConnectionEnum.ERROR);
                                break;
                            }
                        }
                        ctrlSocket.close();
                        ctrlSocketServer.close();
                    }
                    if(dataSocketServer!=null){
                        if(!dataSocketServer.isClosed()){

                            Socket socketFakeData = new Socket("localhost", Constants.IP_CONNECTION.DATAPORT);
                            socketFakeData.setSoTimeout(500);
                            socketFakeData.close();}

                        while(!dataSocketServerDisconnected){
                            try {
                                Thread.sleep(5);
                                //Protect Against endless Loop if its disconnected while RUNNING
                                counter++;
                                if(counter>=5) dataSocketServerDisconnected=true;
                            } catch (InterruptedException e) {
                                Log.i(TAG, "Error while Sleeping during the Stopping Sequence");
                                e.printStackTrace();
                                setIpState(ConnectionEnum.ERROR);
                                break;
                            }
                        }
                        dataSocket.close();
                        dataSocketServer.close();
                    }

                    if(remoteDataSocket!=null){
                        if(!remoteDataSocket.isClosed())
                            dataSocketServerDisconnected=true;

                        remoteDataSocket.close();
                    }

                    Log.d(TAG, "Closed IP Connection Service");

                } catch (IOException e) {
                    Log.d(TAG, "Error on closing IP Connection Service");
                    setIpState(ConnectionEnum.ERROR);
                    e.printStackTrace();
                }
                ipService.setIsClosing(false);
            }
        }, "StopIpConnection").start();
    }

    // Giving information over the Control Channel if the Data Channel is free
    protected boolean requestDataStatus(){

        return (isRunning()||isConnected());
    }

    private void sendData(BufferedWriter outData, String dataTypeMask) throws IOException{

        JSONObject transmit = getDataHandler().getTransmitData(dataTypeMask,requestDataStatus());

        if(transmit != null){
            Log.d(TAG, transmit.toString());
            outData.write(transmit.toString());
            outData.newLine();
            outData.flush();
        }else{
            Log.d(TAG,"Error while Creating JSON Object on Handler");
            setIpState(ConnectionEnum.ERROR);
        }
    }

    protected void receiveData(String dataPacket) throws IOException{
        // Hier kommt noch die Verwertung und Weitergabe von JSON Paketen
        Log.d(TAG,dataPacket);
        getDataHandler().parseJson(dataPacket);
    }

    protected boolean receiveCtrl(String dataPacket) throws IOException{

        Log.d(TAG, dataPacket);
        return getDataHandler().parseJson(dataPacket);
    }

    protected class IpDataConnectionServerThread extends Thread {

        BufferedReader inData;
        BufferedWriter outData;

        public IpDataConnectionServerThread() {
            super("IpConnection-IpDataConnectionServerThread");
            inData = null;
            outData = null;
        }

        @Override
        public void run() {

            dataSocketServerDisconnected=false;

            while(isRunning()||isTryFind()||isConnected()){

                dataSocket = null;

                try {
                    Log.d(TAG, "Waiting for Data Connection Accept");
                    dataSocket = dataSocketServer.accept();

                } catch (IOException e) {
                    setIpState(ConnectionEnum.ERROR);
                    e.printStackTrace();
                }

                if(dataSocket!=null) {
                    if (!String.valueOf(dataSocket.getInetAddress().getHostName()).equals("localhost")){
                        setIpState(ConnectionEnum.CONNECTED);


                        try {
                            inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                            outData = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
                            setIpState(ConnectionEnum.RUNNING);

                            new IpDataSendThread(outData).start();
                            new IpDataReceiveThread(inData).start();
                        } catch (IOException e) {
                            Log.d(TAG, "BufferedReader/Writer Initialization Error");
                            setIpState(ConnectionEnum.ERROR);
                            e.printStackTrace();
                            break;
                        }
                    }else{
                        dataSocketServerDisconnected=true;
                        break;
                    }
                }

                while (isRunning()) {
                    //Wait until the Connection is lost and shown by the Client Receive
                    //Talk about a better solution - or just together with Receive Thread?
                    try {
                        Thread.sleep(5);
                        if(dataSocketServerDisconnected) break;
                    } catch (InterruptedException e) {
                        setIpState(ConnectionEnum.ERROR);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected class IpDataReceiveThread extends Thread {

        BufferedReader inData;

        public IpDataReceiveThread(BufferedReader reader) {
            super("IpConnection-IpDataReceiveThread");
            this.inData = reader;
        }
        @Override
        public void run() {

            while(isRunning())
            {
                try {
                    while(((incomingDataMsg = inData.readLine())!=null)){
                        if(dataSocketServerDisconnected) break;
                        receiveData(incomingDataMsg);
                    }
                    setIpState(ConnectionEnum.TRYFIND);
                } catch (IOException e) {
                    Log.d(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.ERROR);
                    e.printStackTrace();
                }
            }
        }
    }//IpDataReceiveThread

    protected class IpDataSendThread extends Thread {

        BufferedWriter outData;

        public IpDataSendThread(BufferedWriter writer) {
            //super("IpConnection-IpDataSendThread");
            this.outData = writer;
        }
        @Override
        public void run() {

            while(isRunning())
            {
                try {
                    sendData(outData, "Car");
                    //Real time trigger to set up with Max
                    Thread.sleep(1000);
                    if(dataSocketServerDisconnected) break;
                } catch (IOException e) {
                    //This Error will be created be Closing Connection while sleeping
                    Log.d(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.TRYFIND);
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

            ctrlSocketServerDisconnected = false;

            // Stay Rdy for Request of Transmission over Control Server Socket
            while (isRunning()||isTryFind()||isConnected()) {
                ctrlSocket = null;

                try {
                    Log.d(TAG, "Waiting for Ctrl Connection Accept");
                    ctrlSocket = ctrlSocketServer.accept();
                    // new thread for each client request
                } catch (IOException e) {
                    Log.d(TAG, "Error on Ctrl Connection Accept");
                    setIpState(ConnectionEnum.TRYCONNECTERROR);
                    e.printStackTrace();
                }

                if(ctrlSocket!=null) {
                    if (!String.valueOf(ctrlSocket.getInetAddress().getHostName()).equals("localhost"))
                        new CtrlMsgEchoThread(ctrlSocket).start();
                    else {
                        ctrlSocketServerDisconnected = true;
                        break;
                    }
                }else{
                    Log.d(TAG, "Ctrl Socket is null");
                    setIpState(ConnectionEnum.ERROR);
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
                    sendData(dataInfoOut, "");
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

        protected String address;
        protected BufferedReader inData;
        protected BufferedWriter outData;
        protected boolean isConnectedRunning;

        public ClientConnection(String ipAddress){

            this.address = ipAddress;
            inData = null;
            outData = null;
            isConnectedRunning = true;
        }

        public void run(){
            if(isTryConnect()) {
                try {
                    remoteCtrlSocket = new Socket(address, Constants.IP_CONNECTION.CTRLPORT);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(remoteCtrlSocket.getInputStream()));
                    while ((incomingDataMsg = reader.readLine()) != null) {
                        receiveData(incomingDataMsg);

                        if (receiveCtrl(incomingDataMsg)) isConnectedRunning = true;
                        else isConnectedRunning = false;
                    }

                    remoteCtrlSocket.close();

                    try {
                        if (isConnectedRunning == true) {

                            Log.d(TAG, "Data Server is already in use - No Connection has been established");
                        } else if (isConnectedRunning == false) {

                            Log.d(TAG, "Data Server is free - Try to build up a Connection");
                            setIpState(ConnectionEnum.CONNECTED);
                            remoteDataSocket = new Socket(address, Constants.IP_CONNECTION.DATAPORT);
                            //remoteDataSocket = new Socket("192.168.178.24", 12023);

                            inData = new BufferedReader(new InputStreamReader(remoteDataSocket.getInputStream()));
                            outData = new BufferedWriter(new OutputStreamWriter(remoteDataSocket.getOutputStream()));
                            setIpState(ConnectionEnum.RUNNING);

                            new IpDataSendThread(outData).start();
                            new IpDataReceiveThread(inData).start();
                        }
                    } catch (IOException e) {
                        setIpState(ConnectionEnum.TRYCONNECTERROR);
                        Log.d(TAG, "Client cant connect to Data Server Socket");
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    setIpState(ConnectionEnum.ERROR);
                    Log.d(TAG, "Client cant connect to Ctrl Server Socket");
                    e.printStackTrace();
                }
            }else if(isConnected()||isRunning()){
                Log.d(TAG, "Client is already connected");
            }
        }
    }

    protected boolean isIdle() { return getDData().getIpState().isIdle();}
    protected boolean isTryFind() { return getDData().getIpState().isTryFind();}
    protected boolean isFound() { return getDData().getIpState().isFound();}
    protected boolean isTryConnect() { return getDData().getIpState().isTryConnect(); }
    protected boolean isConnected() { return getDData().getIpState().isConnected();}
    protected boolean isRunning() { return getDData().getIpState().isRunning();}
    protected boolean isError() { return getDData().getIpState().isError();}
    protected boolean isUnknown() { return getDData().getIpState().isUnknown();}

    protected synchronized void setIpState(ConnectionEnum state){
        setIpState(state, "");
    }

    protected synchronized void setIpState(ConnectionEnum state, String error){

        if(getDData().getIpState() != null) {
            getDData().setIpState(new ConnectionState(state, error));
            Log.d(TAG, "IP State changed: " + getDData().getIpState().getStateName());

            if (!(  state.equals(getDData().getIpState().getState()) &&
                    error.equals(getDData().getIpState().getError()))) {//if not equal

                getDData().setIpState(new ConnectionState(state, error));
                Log.d(TAG, "serial State changed: " + getDData().getIpState().getStateName());
                if(getDData().getIpState().isError()){
                    Log.e(TAG, error);
                }
                else if(!error.equals("")){
                    Log.w(TAG, error);
                }
            }
            if (ipService != null) {
                Intent onIpConnectionStatusChangeIntent = new Intent(Constants.EVENT.IP_STATUS_CHANGED);
                LocalBroadcastManager.getInstance(ipService).sendBroadcast(onIpConnectionStatusChangeIntent);
            }
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
