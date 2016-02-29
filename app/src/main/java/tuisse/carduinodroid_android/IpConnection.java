package tuisse.carduinodroid_android;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

/**
 * Created by keX on 04.01.2016.
 */
public class IpConnection {

    protected IpService ipService;

    protected IpDataConnectionServerThread ipDataConnectionServerThread;
    protected IpCtrlSendServerThread       ipCtrlSendServerThread;

    static final String TAG = "CarduinoIpConnection";

    HardwareInformation hardwareInformation;
    Sound sound;

    ServerSocket ctrlSocketServer;
    ServerSocket dataSocketServer;

    Socket ctrlSocket;
    Socket dataSocket;
    Socket remoteCtrlSocket;
    Socket remoteDataSocket;

    BufferedWriter intentWriter;

    CameraSupportedResolutionReceiver cameraSupportedResolutionReceiver;
    CameraSettingsChangedReceiver cameraSettingsChangedReceiver;
    SoundSettingChangedReceiver soundSettingChangedReceiver;
    SerialStatusChangedReceiver serialStatusChangedReceiver;
    MobilityGPSChangedReceiver mobilityGPSChangedReceiver;
    FeatureChangedReceiver featureChangedReceiver;

    IntentFilter cameraSupportedResolutionFilter;
    IntentFilter cameraSettingsChangedFilter;
    IntentFilter soundSettingChangedFilter;
    IntentFilter serialStatusChangedFilter;
    IntentFilter mobilityGpChangedFilter;
    IntentFilter featureChangedFilter;

    protected boolean isClient;

    protected boolean ctrlSocketServerDisconnected;
    protected boolean dataSocketServerDisconnected;
    protected boolean dataReceiveDisconnected;
    protected boolean dataSendDisconnected;

    private String incomingDataMsg;

    IpConnection(IpService s){

        ipService = s;

        sound = new Sound();
        hardwareInformation = new HardwareInformation(ipService);

        cameraSupportedResolutionReceiver = new CameraSupportedResolutionReceiver();
        cameraSettingsChangedReceiver = new CameraSettingsChangedReceiver();
        soundSettingChangedReceiver = new SoundSettingChangedReceiver();
        serialStatusChangedReceiver = new SerialStatusChangedReceiver();
        mobilityGPSChangedReceiver = new MobilityGPSChangedReceiver();
        featureChangedReceiver = new FeatureChangedReceiver();

        cameraSupportedResolutionFilter = new IntentFilter(Constants.EVENT.CAMERA_SUPPORTED_RESOLUTION);
        cameraSettingsChangedFilter = new IntentFilter(Constants.EVENT.CAMERA_SETTINGS_CHANGED);
        soundSettingChangedFilter = new IntentFilter(Constants.EVENT.SOUND_PLAY_CHANGED);
        serialStatusChangedFilter = new IntentFilter(Constants.EVENT.SERIAL_STATUS_CHANGED);
        mobilityGpChangedFilter = new IntentFilter(Constants.EVENT.MOBILITY_GPS_CHANGED);
        featureChangedFilter = new IntentFilter(Constants.EVENT.FEATURES_CHANGED);

        LocalBroadcastManager.getInstance(ipService).registerReceiver(cameraSupportedResolutionReceiver, cameraSupportedResolutionFilter);
        LocalBroadcastManager.getInstance(ipService).registerReceiver(cameraSettingsChangedReceiver, cameraSettingsChangedFilter);
        LocalBroadcastManager.getInstance(ipService).registerReceiver(soundSettingChangedReceiver, soundSettingChangedFilter);
        LocalBroadcastManager.getInstance(ipService).registerReceiver(serialStatusChangedReceiver, serialStatusChangedFilter);
        LocalBroadcastManager.getInstance(ipService).registerReceiver(mobilityGPSChangedReceiver, mobilityGpChangedFilter);
        LocalBroadcastManager.getInstance(ipService).registerReceiver(featureChangedReceiver, featureChangedFilter);

        reset();
    }

    protected void reset(){

        ctrlSocketServer = null;
        dataSocketServer = null;
        ctrlSocket = null;
        dataSocket = null;
        intentWriter = null;
    }

    protected DataHandler getDataHandler(){
        return ipService.getCarduino().dataHandler;
    }

    //Initialization of both ServerSockets if a connection via another mobile phone or pc is wished
    protected void initServer(){

        isClient = false;

        setMyIp(hardwareInformation.getLocalIpAdress());
        setRemoteIP("Not Connected");

        ipDataConnectionServerThread = new IpDataConnectionServerThread();
        ipCtrlSendServerThread = new IpCtrlSendServerThread();

        try {
            createSocketServer(Constants.IP_CONNECTION.TAG_CTRLPORT);
            createSocketServer(Constants.IP_CONNECTION.TAG_DATAPORT);
        } catch (IOException e) {
            Log.e(TAG, "Problem with Setup of DATA or CONTROL Server Socket");
            setIpState(ConnectionEnum.ERROR, "SocketServer Setup Failure");
            e.printStackTrace();
        }
    }

    //Creates a SocketServer depending on the expected Type (Data,Ctrl)
    protected boolean createSocketServer(String socketType) throws IOException {

        if(socketType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_DATAPORT.toLowerCase()))
        {
            if(dataSocketServer==null){

                Log.i(TAG, "Create Data Server Socket");
                dataSocketServer = new ServerSocket();
                dataSocketServer.setReuseAddress(true);
                dataSocketServer.bind(new InetSocketAddress(Constants.IP_CONNECTION.DATAPORT));

                Log.i(TAG, "Created Data Server Socket");
            }else{Log.i(TAG, "Data Server Socket was already initialized");}

        }else if(socketType.toLowerCase().equals(Constants.IP_CONNECTION.TAG_CTRLPORT.toLowerCase()))
        {
            if(ctrlSocketServer==null) {

                Log.i(TAG, "Create Ctrl Server Socket");
                ctrlSocketServer = new ServerSocket();
                ctrlSocketServer.setReuseAddress(true);
                ctrlSocketServer.bind(new InetSocketAddress(Constants.IP_CONNECTION.CTRLPORT));
                Log.i(TAG, "Created Ctrl Server Socket");
            }else{Log.i(TAG, "Ctrl Server Socket was already initialized");}

        }else{
            Log.e(TAG, "Server Socket has the wrong type");
            return false;
        }

        return true;
    }

    protected void startServerThread(String dataType){

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
            setIpState(ConnectionEnum.ERROR, "Data or Ctrl Socket not Available");
            Log.e(TAG, "Data/Ctrl Socket not available");
        }
    }

    protected void initClient(){

        isClient = true;
        setMyIp(hardwareInformation.getLocalIpAdress());

        setIpState(ConnectionEnum.TRYCONNECT);
    }

    protected void startClientThread(){

        new ClientConnection().start();
    }

    protected void close(){

        Log.i(TAG, "Closing IP Connection Service");

        new Thread(new Runnable() {

            protected int counter = 0;

            public void run() {
                try {
                    //hard work around for client.accept to cancel them without exception -
                    //to not miss use an important exception while waiting for connection
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
                                Log.e(TAG, "Error while Sleeping during the Stopping Sequence");
                                e.printStackTrace();
                                setIpState(ConnectionEnum.ERROR, "Sleeping Error Ctrl while Stopping");
                                break;
                            }
                        }
                        if(ctrlSocket!=null)ctrlSocket.close();
                        if(ctrlSocketServer!=null)ctrlSocketServer.close();
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
                                if(counter>=5){
                                    dataSocketServerDisconnected=true;
                                    Log.i(TAG,"Data Socket disconnect Counter expired");
                                }
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Error while Sleeping during the Stopping Sequence");
                                e.printStackTrace();
                                setIpState(ConnectionEnum.ERROR, "Sleeping Error Data while Stopping");
                                break;
                            }
                        }
                        if(dataSocket!=null)dataSocket.close();
                        if(dataSocketServer!=null)dataSocketServer.close();
                    }

                    if(remoteDataSocket!=null){
                        if(!remoteDataSocket.isClosed())
                            dataSocketServerDisconnected=true;
                        while(!dataReceiveDisconnected&&!dataSendDisconnected){
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "Client Threads Closed");
                        if(!remoteDataSocket.isClosed())remoteDataSocket.close();
                    }

                    Log.i(TAG, "Closed IP Connection Service");

                } catch (IOException e) {
                    Log.e(TAG, "Error on closing IP Connection Service");
                    setIpState(ConnectionEnum.ERROR, "IP Connection Service Closing Failure");
                    e.printStackTrace();
                }
                ipService.setIsClosing(false);

                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(cameraSupportedResolutionReceiver);
                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(cameraSettingsChangedReceiver);
                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(soundSettingChangedReceiver);
                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(serialStatusChangedReceiver);
                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(mobilityGPSChangedReceiver);
                LocalBroadcastManager.getInstance(ipService).unregisterReceiver(featureChangedReceiver);

                intentWriter = null;
            }
        }, "StopIpConnection").start();
    }

    // Giving information over the Control Channel if the Data Channel is free
    protected boolean requestDataStatus(){

        return (isRunning()||isConnected());
    }

    private synchronized void sendData(BufferedWriter outData, String dataTypeMask) throws IOException{

        JSONObject transmit = getDataHandler().getTransmitData(dataTypeMask, requestDataStatus());

        if(transmit != null){
            if(Constants.LOG.IP_SENDER) {
                Log.i(TAG,  "OUT: " +  transmit.toString());
            }
            outData.write(transmit.toString());
            outData.newLine();
            outData.flush();
        }else{
            Log.e(TAG,"Error while Creating JSON Object on Handler");
            setIpState(ConnectionEnum.ERROR, "Data sending Failure");
        }
    }

    protected void receiveData(String dataPacket) throws IOException{

        String mask = getDataHandler().parseJson(dataPacket);
        if(Constants.LOG.IP_RECEIVER) {
            Log.i(TAG, "IN: " + dataPacket.toString());
        }

        if(mask.contains(Constants.JSON_OBJECT.NUM_CAR)){
            Intent onIpDataCar = new Intent(Constants.EVENT.IP_DATA_CAR);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onIpDataCar);
        }

        if(mask.contains(Constants.JSON_OBJECT.NUM_VIDEO)){
            Intent onIpDataVideo = new Intent(Constants.EVENT.IP_DATA_VIDEO);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onIpDataVideo);
        }

        if(mask.contains(Constants.JSON_OBJECT.NUM_SERIAL)){
            if(isClient){
                Intent onSerialStatusIntent = new Intent(Constants.EVENT.SERIAL_STATUS_CHANGED);
                LocalBroadcastManager.getInstance(ipService).sendBroadcast(onSerialStatusIntent);
            }
        }

        if(mask.contains(Constants.JSON_OBJECT.NUM_CONTROL)){
            Intent onIpDataControl = new Intent(Constants.EVENT.IP_DATA_CONTROL);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onIpDataControl);
        }

        if(mask.contains(Constants.JSON_OBJECT.NUM_CAMERA)){
            Intent onIpDataCamera = new Intent(Constants.EVENT.IP_DATA_CAMERA);
            LocalBroadcastManager.getInstance(ipService).sendBroadcast(onIpDataCamera);}

        if(mask.contains(Constants.JSON_OBJECT.NUM_SOUND)){
            if(getDData().getSoundPlay() == 1){
                sound.horn();
            } else{
                sound.stop();
            }
        }
    }

    protected String receiveCtrl(String dataPacket) throws IOException{

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

                    Log.i(TAG, "Waiting for Data Connection Accept");
                    dataSocket = dataSocketServer.accept();

                    if(dataSocket!=null) {
                        if (!String.valueOf(dataSocket.getInetAddress().getHostName()).equals("localhost")){
                            setIpState(ConnectionEnum.CONNECTED);
                            setRemoteIP(dataSocket.getInetAddress().getHostAddress());

                            try {
                                inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                                outData = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
                                setIpState(ConnectionEnum.RUNNING);

                                new IpDataSendThread(outData).start();
                                new IpDataReceiveThread(inData).start();

                                saveActualBufferedWriter(outData);
                            } catch (IOException e) {
                                Log.e(TAG, "BufferedReader/Writer Initialization Error");
                                setIpState(ConnectionEnum.ERROR, "BufferedReader/Writer Init Error");
                                e.printStackTrace();
                                break;
                            }
                        }else{
                            dataSocketServerDisconnected=true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    setIpState(ConnectionEnum.ERROR, "Error on Accept Remote Data Connection");
                    e.printStackTrace();
                }

                while (isRunning()) {
                    //Wait until the Connection is lost and shown by the Client Receive
                    //Talk about a better solution - or just together with Receive Thread?
                    try {
                        Thread.sleep(5);
                        if(dataSocketServerDisconnected) break;
                    } catch (InterruptedException e) {
                        setIpState(ConnectionEnum.ERROR, "Error on Data SocketServer Sleep");
                        e.printStackTrace();
                    }
                }
                saveActualBufferedWriter(null);
                if(dataSocketServerDisconnected) break;
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
            dataReceiveDisconnected = false;
            while(isRunning())
            {
                try {
                    while(((incomingDataMsg = inData.readLine())!=null)){
                        if(dataSocketServerDisconnected) break;
                        receiveData(incomingDataMsg);
                    }

                    if(isClient) {
                        setIpState(ConnectionEnum.TRYCONNECT);
                    }
                    else{
                        setIpState(ConnectionEnum.TRYFIND);
                        setRemoteIP("Not Connected");
                    }
                    saveActualBufferedWriter(null);
                    break;
                } catch (IOException e) {
                    Log.e(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.ERROR, "Receive Thread Error");
                    e.printStackTrace();
                    break;
                }
            }
            //Reset Values if Connection is lost and maybe got back to Setup lowest Quality for Safety
            getDData().resetValues();
            dataReceiveDisconnected = true;
        }
    }//IpDataReceiveThread

    protected class IpDataSendThread extends Thread {

        BufferedWriter outData;
        int timer = 0;
        String information;
        public IpDataSendThread(BufferedWriter writer) {
            this.outData = writer;
        }
        @Override
        public void run() {
            dataSendDisconnected = false;
            if(!isClient){
                try{
                    sendData(outData,Constants.JSON_OBJECT.NUM_SERIAL);
                }
                catch (IOException e) {
                    Log.i(TAG, "Already Connection Lost before send");
                    setIpState(ConnectionEnum.TRYFIND);
                    setRemoteIP("Not Connected");
                    e.printStackTrace();
                }
            }
            while(isRunning())
            {
                try {

                    information = "";

                    if(!isClient){ //Data from Transceiver to Remote
                        //every 50 ms = DELAY.IP_SENDER(check Constants)
                        information +=
                                Constants.JSON_OBJECT.NUM_VIDEO;
                        //every 2 * 50 ms = 100 ms
                        if((timer = timer % Constants.DELAY.FACTOR_CAR) == 0){

                            information +=
                                    Constants.JSON_OBJECT.NUM_CAR;
                        }
                    }
                    else{ //Data from Remote to Transceiver
                        //every 2 * 50 ms = 100 ms
                        if((timer = timer % Constants.DELAY.FACTOR_CONTROL) == 0){

                            information +=
                                    Constants.JSON_OBJECT.NUM_CONTROL;
                        }
                    }
                    //Check if the information is empty - so no need to send
                    if(!information.equals("")) sendData(outData,information);

                    Thread.sleep(Constants.DELAY.IP);
                    timer++;
					
                    if(dataSocketServerDisconnected) break;
                } catch (IOException e) {
                    //This Error will be created be Closing Connection while sleeping
                    Log.i(TAG, "Already Connection Lost before send");
                    if(isClient){
                        setIpState(ConnectionEnum.TRYCONNECT);
                    }
                    else {
                        setIpState(ConnectionEnum.TRYFIND);
                        setRemoteIP("Not Connected");
                    }
                    e.printStackTrace();
                    break;
                } catch (InterruptedException e) {
                    setIpState(ConnectionEnum.ERROR, "Receive Thread Sleep Error");
                    e.printStackTrace();
                    break;
                }
            }
            //Reset Values if Connection is lost and maybe got back to Setup lowest Quality for Safety
            getDData().resetValues();
            saveActualBufferedWriter(null);
            dataSendDisconnected = true;
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
                    Log.i(TAG, "Waiting for Ctrl Connection Accept");
                    ctrlSocket = ctrlSocketServer.accept();
                    // new thread for each client request
                } catch (IOException e) {
                    Log.e(TAG, "Error on Ctrl Connection Accept");
                    setIpState(ConnectionEnum.TRYCONNECTERROR, "Error on Accept Remote Ctrl Connection");
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
                    Log.e(TAG, "Ctrl Socket is null");
                    setIpState(ConnectionEnum.ERROR, "Ctrl Socket already Null");
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
                Log.e(TAG, "Ctrl Msg wasnt send");
                setIpState(ConnectionEnum.TRYCONNECTERROR, "Error on Sending Ctrl Message");
                e.printStackTrace();
            } catch (InterruptedException e) {
                setIpState(ConnectionEnum.ERROR, "Error on Ctrl Message Sleep");
                e.printStackTrace();
            }
        }
    }

    public class ClientConnection extends Thread{

        protected String address;
        protected BufferedReader inData;
        protected BufferedWriter outData;
        protected boolean isTargetDataSocketInUse;

        public ClientConnection(){

            inData = null;
            outData = null;
            isTargetDataSocketInUse = true;
        }

        public void run(){
            while(isConnected() || isRunning() || isTryConnect()) {
                if (isTryConnect()) {
                    try {
                        address = getTransceiverIP();

                        remoteCtrlSocket = new Socket(address, Constants.IP_CONNECTION.CTRLPORT);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(remoteCtrlSocket.getInputStream()));
                        while ((incomingDataMsg = reader.readLine()) != null) {
                            receiveData(incomingDataMsg);

                            if (receiveCtrl(incomingDataMsg).equals("false")) isTargetDataSocketInUse = false;
                            else isTargetDataSocketInUse = true;
                        }

                        remoteCtrlSocket.close();

                        try {
                            if (isTargetDataSocketInUse) {

                                Log.i(TAG, "Data Server is already in use - No Connection has been established");
                            } else if (!isTargetDataSocketInUse) {

                                Log.i(TAG, "Data Server is free - Try to build up a Connection");
                                setIpState(ConnectionEnum.CONNECTED);
                                remoteDataSocket = new Socket(address, Constants.IP_CONNECTION.DATAPORT);

                                inData = new BufferedReader(new InputStreamReader(remoteDataSocket.getInputStream()));
                                outData = new BufferedWriter(new OutputStreamWriter(remoteDataSocket.getOutputStream()));
                                setIpState(ConnectionEnum.RUNNING);

                                new IpDataSendThread(outData).start();
                                new IpDataReceiveThread(inData).start();
                                // Here for intent Sending on Remote side
                                saveActualBufferedWriter(outData);
                            }
                        } catch (IOException e) {
                            setIpState(ConnectionEnum.TRYCONNECTERROR, "Client can't connect to Data");
                            Log.e(TAG, "Client cant connect to Data Server Socket");
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        setIpState(ConnectionEnum.ERROR, "Client can't connect to Ctrl");
                        Log.e(TAG, "Client cant connect to Ctrl Server Socket");
                        e.printStackTrace();
                    }
                } else if (isConnected() || isRunning()) {
                    if(!address.equals(getTransceiverIP())) {
                        address = getTransceiverIP();
                        if(remoteDataSocket!=null){
                            if(!remoteDataSocket.isClosed())
                                dataSocketServerDisconnected=true;
                            while(!dataReceiveDisconnected&&!dataSendDisconnected){
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.i(TAG, "Client Threads Closed");
                            if(!remoteDataSocket.isClosed()) try {
                                remoteDataSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        //Log.d(TAG, "Client is already running");
                    }
                }

                try {
                    Thread.sleep(Constants.DELAY.CONNECTIONTRY);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error while Setting up the next Connection Try");
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveActualBufferedWriter(BufferedWriter writer) {
        intentWriter = writer;
    }

    private class CameraSupportedResolutionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            sendData(intentWriter, Constants.JSON_OBJECT.NUM_HARDWARE);
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Supported Resolutions");
                            e.printStackTrace();
                        }
                }
            }, "CameraSupportedSizesThread").start();
        }
    }

    private class CameraSettingsChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            sendData(intentWriter, Constants.JSON_OBJECT.NUM_CAMERA);
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Camera Settings");
                            e.printStackTrace();
                        }
                }
            }, "CameraSettingsUpdateThread").start();
        }
    }

    private class SoundSettingChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            sendData(intentWriter, Constants.JSON_OBJECT.NUM_SOUND);
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Sound Settings");
                            e.printStackTrace();
                        }
                }
            }, "SoundPlayUpdateThread").start();
        }
    }

    private class SerialStatusChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            if(!isClient) {
                                sendData(intentWriter, Constants.JSON_OBJECT.NUM_SERIAL);
                                Log.d(TAG,"send serial status changed event");
                            }
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Serial Status");
                            e.printStackTrace();
                        }
                }
            }, "SerialStatusUpdateThread").start();
        }
    }

    private class MobilityGPSChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            sendData(intentWriter, Constants.JSON_OBJECT.NUM_MOBILITY);
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Mobility Information");
                            e.printStackTrace();
                        }
                }
            }, "MobilityUpdateThread").start();
        }
    }

    private class FeatureChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                public void run() {
                    if(intentWriter != null)
                        try {
                            sendData(intentWriter, Constants.JSON_OBJECT.NUM_FEATURES);
                        } catch (IOException e) {
                            Log.e(TAG,"Error on Using Intent Sending for Features Update");
                            e.printStackTrace();
                        }
                }
            }, "FeatureUpdateThread").start();
        }
    }


    protected void setRemoteIP(String ip){
        getDData().setRemoteIp(ip);
    }

    protected void setTransceiverIP(String ip){
        getDData().setTransceiverIp(ip);
    }
    protected String getTransceiverIP(){
        return getDData().getTransceiverIp();
    }

    protected void setMyIp(String ip){
        getDData().setMyIp(ip);
    }

	protected boolean isIdle() { return getDData().getIpState().isIdle();}
	protected boolean isTryFind() { return getDData().getIpState().isTryFind();}
	protected boolean isFound() { return getDData().getIpState().isFound();}
	protected boolean isTryConnect() { return getDData().getIpState().isTryConnect(); }
	protected boolean isConnected() { return getDData().getIpState().isConnected();}
	protected boolean isRunning() { return getDData().getIpState().isRunning();}
	protected boolean isError() { return getDData().getIpState().isError();}

	
	protected synchronized void setIpState(ConnectionEnum state){
        setIpState(state, "");
	}

    protected synchronized void setIpState(ConnectionEnum state, int error){
        setIpState(state, ipService.getString(error));
    }

    protected synchronized void setIpState(ConnectionEnum state, String error){

        if(getDData().getIpState() != null) {
            getDData().setIpState(new ConnectionState(state, error));
            Log.d(TAG, "IP_SENDER State changed: " + getDData().getIpState().getStateName());

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
