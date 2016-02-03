package tuisse.carduinodroid_android;


import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import tuisse.carduinodroid_android.data.CarduinoData;
import tuisse.carduinodroid_android.data.CarduinoDroidData;
import tuisse.carduinodroid_android.data.IpType;

/**
 * Created by keX on 04.01.2016.
 */
public class IpConnection {
    protected IpService ipService;

    protected IpDataReceiveThread       ipDataReceiveThread;
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
    BufferedReader inCtrl;
    BufferedReader inData;

    private String incomingCtrlMsg;
    private String outgoingCtrlMsg;
    private String incomingDataMsg;
    private String outgoingDataMsg;

    IpConnection(IpService s){

        ipService = s;

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
        inCtrl = null;
        inData = null;

        ctrlStatusSend = false;
    }
    //Initialization of both ServerSockets if a connection via another mobile phone or pc is wished
    protected boolean init(){

        //reset();

        try {
            createSocketServer(TAG_CTRLPORT);
            createSocketServer(TAG_DATAPORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected void startThread(String dataType){
        Log.d(TAG, "Start Server Threads and BufferedWriter/Reader");

        if(dataType.toLowerCase().equals(TAG_CTRLPORT.toLowerCase()))
        {
            try {
                ctrlClient = ctrlSocket.accept();
                Log.d(TAG, "Ctrl ClientSocket Accepted");

                outCtrl = new BufferedWriter(new OutputStreamWriter(ctrlClient.getOutputStream()));
                inCtrl = new BufferedReader(new InputStreamReader(ctrlClient.getInputStream()));
                Log.d(TAG, "Ctrl BufferedWriter/Reader Created");


            } catch (IOException e) {
                Log.d(TAG, "Fehler Ctrl ClientSocket Accept");
                e.printStackTrace();
            }

            if(!ipCtrlSendThread.isAlive()){

                Log.d(TAG, "Send Thread Started for Ctrl Income");
                ipCtrlSendThread.start();
            }

        }else if(dataType.toLowerCase().equals(TAG_DATAPORT.toLowerCase()))
        {
            try {
                dataClient = dataSocket.accept();
                Log.d(TAG, "Data ClientSocket Accepted");

                outData = new BufferedWriter(new OutputStreamWriter(dataClient.getOutputStream()));
                inData = new BufferedReader(new InputStreamReader(dataClient.getInputStream()));
                Log.d(TAG, "Data BufferedWriter/Reader Created");

            } catch (IOException e) {
                Log.d(TAG, "Fehler Data ClientSocket Accept");
                e.printStackTrace();
            }

            if(!ipDataReceiveThread.isAlive()){

                Log.d(TAG, "Receive Thread Started for Data Income");
                ipDataReceiveThread.start();
            }

            if(!ipDataSendThread.isAlive()){

                Log.d(TAG, "Sent Thread Started for Data Income");
                ipDataSendThread.start();
            }

        }
        else Log.d(TAG, "Data/Ctrl Socket nicht da");

    }

    protected void closeThreads(){
        Log.d(TAG, "Reset Server Thread");
        ipDataReceiveThread.interrupt();
        ipCtrlSendThread.interrupt();
        ipDataSendThread.interrupt();
    }

    protected void close(){

        Log.d(TAG, "Stop Server Thread");

    }

    // Giving information over the Control Channel if the Data Channel is free
    protected boolean request(){

        return true;
    }

    protected void sendCtrl() throws IOException{
        //Anpassen dass der Status über JSON vom Data SocketServer gesendet wird
        outCtrl.write("Test");
        outCtrl.newLine();
        outCtrl.flush();
    }

    protected void sendData() throws IOException{

    }

    protected void receiveData() throws IOException{

    }

    protected class IpDataReceiveThread extends Thread {
        public IpDataReceiveThread() {
            super("IpConnection-IpDataReceiveThread");
        }
        @Override
        public void run() {
            Log.d(TAG, "IPDataReceiveThread started");
            try {

                    while ((incomingDataMsg = inData.readLine()) != null) {
                        Log.d(TAG, incomingDataMsg);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }//IpDataReceiveThread

    protected class IpDataSendThread extends Thread {
        public IpDataSendThread() {
            super("IpConnection-IpDataSendThread");
        }
        @Override
        public void run() {
            Log.d(TAG, "IPDataSendThread started");

        }
    }//IpDataSendThread

    protected class IpCtrlSendThread extends Thread {
        public IpCtrlSendThread() {
            super("IpConnection-IpCtrlSendThread");
        }
        @Override
        public void run() {
            Log.d(TAG, "IPCtrlSendThread started");
            try {

                sendCtrl();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//IpSendThread

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

    protected boolean closeBufferReaderWriter(){


        return true;
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
        if(ipService.getCarduino().dataHandler.getData() instanceof CarduinoDroidData){
            return (CarduinoDroidData) ipService.getCarduino().dataHandler.getData();
        }
        Log.e(TAG,"wrong Data");
        return null;
    }

    protected synchronized CarduinoData getData(){
        return ipService.getCarduino().dataHandler.getData();
    }
}
