package tuisse.carduinodroid_android.data;


import android.util.Log;

import org.json.JSONObject;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;

/**
 * Created by keX on 10.12.2015.
 */
public class DataHandler implements SerialFrameIF,IpFrameIF{
    private final String TAG = "CarduinoDataHandler";

    protected CarduinoData cd;
    protected CarduinoDroidData ccd;
    protected SerialFrameHandler serialFrameHandler;
    protected IpFrameHandler ipFrameHandler;

    private ControlMode controlMode = null;
    private SerialType serialPref = SerialType.NONE;
    private String bluetoothDeviceName = "";
    private BluetoothHandling bluetoothHandling = BluetoothHandling.AUTO;
    private boolean failSafeStopPref = true;
    private boolean debugView = false;
    private boolean bluetoothEnabled = false;
    private CommunicationStatus communicationStatus = CommunicationStatus.IDLE;
    private int screensaver = 60000;

    public DataHandler() {
    }

    public synchronized CarduinoData getData(){
        return cd;
    }

    public synchronized CarduinoDroidData getDData(){
        return ccd;
    }

    public synchronized void setScreensaver(int ss){
        screensaver = ss;
    }
    public synchronized int getScreensaver(){
        return screensaver;
    }

    public synchronized void setBluetoothEnabled(boolean bte){
        bluetoothEnabled = bte;
    }
    public synchronized boolean getBluetoothEnabled(){
        return bluetoothEnabled;
    }

    public synchronized CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    public synchronized void calcCommunicationStatus(){
        switch (controlMode){
            case REMOTE:
                if(getDData().getIpState().isRunning()){
                    if(getData().getSerialState().isRunning()){
                        communicationStatus = CommunicationStatus.OK;
                        return;
                    }
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                    return;
                }
                if(getDData().getIpState().isError()){
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.BOTH_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_ERROR;
                    return;
                }
                if(getDData().getIpState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else{
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_CONNECTING;
                    return;
                //}
            case TRANSCEIVER:
                if(getDData().getIpState().isRunning()){
                    if(getData().getSerialState().isRunning()){
                        communicationStatus = CommunicationStatus.OK;
                        return;
                    }
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                    return;
                }
                if(getDData().getIpState().isError()){
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.BOTH_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_ERROR;
                    return;
                }
                if(getDData().getIpState().isIdle() && getData().getSerialState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else{
                if(getData().getSerialState().isRunning()){
                    communicationStatus = CommunicationStatus.IP_CONNECTING;
                    return;
                }
                if(getData().getSerialState().isError()){
                    communicationStatus = CommunicationStatus.SERIAL_ERROR;
                    return;
                }
                communicationStatus = CommunicationStatus.BOTH_CONNECTING;
                return;
            //}
            default://DIRECT
                if(getData().getSerialState().isRunning()){
                    communicationStatus = CommunicationStatus.OK;
                    return;
                }
                if(getData().getSerialState().isError()){
                    communicationStatus = CommunicationStatus.SERIAL_ERROR;
                    return;
                }
                if( getData().getSerialState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else
                communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                return;
        }
    }

    public synchronized int getCommunicationStatusColor(){
        int color = R.color.colorCommunicationStatusOk;
        if(getCommunicationStatus().isIdleError()){
            color = R.color.colorCommunicationStatusIdleError;
        }
        if(getCommunicationStatus().isConnecting()){
            color = R.color.colorCommunicationStatusConnecting;
        }
        return color;
    }

    public synchronized String getCommunicationStatusString(){
        String s = "";
        switch (communicationStatus){
            case IDLE:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusIdle);
                break;
            case BOTH_CONNECTING:
            case IP_CONNECTING:
            case SERIAL_CONNECTING:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusConnecting);
                break;
            case OK:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusOk);
                break;
            case ERROR:
            case BOTH_ERROR:
            case IP_ERROR:
            case SERIAL_ERROR:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusError);
                break;
            default:
                s = "";
                Log.e(TAG, "getCommunicationStatusString: unknown communication status");
                break;
        }
        return s;
    }

    public synchronized void setControlMode(ControlMode cm){
        try {
            if (controlMode == null || cd == null) {
                cd = new CarduinoData();
                //if (cm.isDirect()) {
                //    ccd = null;
                //} else {
                ccd = new CarduinoDroidData();
                //}
            }
            else {
                switch (controlMode) {
                    case TRANSCEIVER: {
                        switch (cm) {
                            case TRANSCEIVER:
                                //do nothing
                                break;
                            case REMOTE:
                                cd = new CarduinoData();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = new CarduinoDroidData();
                                break;
                            default://DIRECT
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //cd stays the same
                                //ccd = null;
                                break;
                        }
                        break;
                    }
                    case REMOTE: {
                        switch (cm) {
                            case TRANSCEIVER:
                                cd = new CarduinoData();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = new CarduinoDroidData();
                                break;
                            case REMOTE:
                                //do nothing
                                break;
                            default://DIRECT
                                cd = new CarduinoData();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = null;
                                break;
                        }
                        break;
                    }
                    default: {//Direct
                        switch (cm) {
                            case TRANSCEIVER:
                                //cd stays the same
                                //ccd = new CarduinoDroidData();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                break;
                            case REMOTE:
                                cd = new CarduinoData();
                                //ccd = new CarduinoDroidData();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                break;
                            default://DIRECT
                                //do nothing
                                break;
                        }
                        break;
                    }
                }
            }
            controlMode = cm;
            if(cd == null){
                Log.e(TAG, "no CarduinoData");
            }
            serialFrameHandler = new SerialFrameHandler(cd);
            if(!controlMode.isDirect()){
                if(ccd == null){
                    Log.e(TAG, "no CarduinoDroidData");
                }
                ipFrameHandler = new IpFrameHandler(cd,ccd);
            }
            else{
                ipFrameHandler = null;
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    public synchronized ControlMode getControlMode(){
        return controlMode;
    }

    public synchronized int setControlModeNext(){
        switch (controlMode){
            case TRANSCEIVER:
                setControlMode(ControlMode.REMOTE);
                break;
            case REMOTE:
                setControlMode(ControlMode.DIRECT);
                break;
            case DIRECT:
                setControlMode(ControlMode.TRANSCEIVER);
                break;
            default:
                setControlMode(ControlMode.TRANSCEIVER);
                Log.e(TAG, "error in setControlModeNext, set ControlMode.Transceiver");
                break;
        }
        return ControlMode.toInteger(controlMode);
    }
    public synchronized int setControlModePrev(){
        int cm;
        try {
            switch (controlMode){
                case TRANSCEIVER:
                    setControlMode(ControlMode.DIRECT);
                    break;
                case REMOTE:
                    setControlMode(ControlMode.TRANSCEIVER);
                    break;
                case DIRECT:
                    setControlMode(ControlMode.REMOTE);
                    break;
                default:
                    Log.e(TAG, "error in setControlModePrev, set ControlMode.Transceiver");
                    setControlMode(ControlMode.TRANSCEIVER);
                    break;
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }finally {
             cm = ControlMode.toInteger(controlMode);
        }
        return cm;
    }

    public synchronized void setFailSafeStopPref(boolean fss){
        failSafeStopPref = fss;
    }

    public synchronized final boolean getFailSafeStopPref(){
        return failSafeStopPref;
    }
    public synchronized int toggleFailSafeStopPref(){
        failSafeStopPref = !failSafeStopPref;
        if(failSafeStopPref){
            return 1;
        }
        return 0;
    }
    public synchronized void setDebugView(boolean dv){
        debugView = dv;
    }
    public synchronized final boolean getDebugView(){
        return debugView;
    }
    public synchronized int toggleDebugView(){
        debugView = !debugView;
        if(debugView){
            return 1;
        }
        return 0;
    }

    public synchronized final BluetoothHandling getBluetoothHandling(){
        return bluetoothHandling;
    }
    public synchronized void setBluetoothHandling(BluetoothHandling bth){
        bluetoothHandling = bth;
    }

    public synchronized final SerialType getSerialPref(){
        return serialPref;
    }
    public synchronized void setSerialPref(SerialType t){
        serialPref = t;
    }

    public synchronized void setBluetoothDeviceName(String name){
        bluetoothDeviceName = name;
    }

    public synchronized final String getBluetoothDeviceName(){
        return bluetoothDeviceName;
    }

    public synchronized int toggleSerialType(ConnectionState serialState){
        if(controlMode.isTransceiver()) {
            if (serialState.isIdleError()) {
                if (serialPref.isBluetooth()) {
                    setSerialPref(SerialType.USB);

                } else {
                    setSerialPref(SerialType.BLUETOOTH);
                }
            }
        }
        return SerialType.toInteger(serialPref);
    }

    public synchronized byte[] serialFrameAssembleTx() {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAssembleTx: " + controlMode.toString());
            return new byte[0];
        }
        else{
            return serialFrameHandler.serialFrameAssembleTx();
        }
    }

    public synchronized boolean serialFrameAppendRx(byte inChar) {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAppendRx: " + controlMode.toString());
            return false;
        }
        else{
            return serialFrameHandler.serialFrameAppendRx(inChar);
        }
    }

    public synchronized boolean parseJson(String jsonObjectRxData) {
        if(controlMode.isDirect()){
            return false;
        }
        return ipFrameHandler.parseJson(jsonObjectRxData);
    }

    @Override
    public JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus) {
        if(controlMode.isDirect()){
            return null;
        }
        return ipFrameHandler.getTransmitData(dataTypeMask,dataServerStatus);
    }
}