package tuisse.carduinodroid_android.data;


import android.util.Log;

import org.json.JSONObject;

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

    public DataHandler() {
    }

    public CarduinoData getData(){
        return cd;
    }

    public CarduinoDroidData getDData(){
        return ccd;
    }

    public synchronized void setBluetoothEnabled(boolean bte){
        bluetoothEnabled = bte;
    }
    public synchronized boolean getBluetoothEnabled(){
        return bluetoothEnabled;
    }

    public synchronized CommunicationStatus getCommunicationStatus() {
        //TODO: implement
        return CommunicationStatus.NONE;
    }

    public synchronized void setControlMode(ControlMode cm){
        try {
            if (controlMode == null || cd == null) {
                cd = new CarduinoData();
                if (cm.isDirect()) {
                    ccd = null;
                } else {
                    ccd = new CarduinoDroidData();
                }
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
                                ccd = new CarduinoDroidData();
                                break;
                            default://DIRECT
                                cd = new CarduinoData(cd);
                                ccd = null;
                                break;
                        }
                        break;
                    }
                    case REMOTE: {
                        switch (cm) {
                            case TRANSCEIVER:
                                cd = new CarduinoData();
                                ccd = new CarduinoDroidData();
                                break;
                            case REMOTE:
                                //do nothing
                                break;
                            default://DIRECT
                                cd = new CarduinoData();
                                ccd = null;
                                break;
                        }
                        break;
                    }
                    default: {//Direct
                        switch (cm) {
                            case TRANSCEIVER:
                                cd = new CarduinoData(cd);
                                ccd = new CarduinoDroidData();
                                break;
                            case REMOTE:
                                cd = new CarduinoData();
                                ccd = new CarduinoDroidData();
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

    public synchronized boolean parseJson(JSONObject jsonObjectRxData) {
        if(controlMode.isDirect()){
            return false;
        }
        return ipFrameHandler.parseJson(jsonObjectRxData);
    }

    public synchronized boolean createJsonObject(String dataTypeMask, String transmitData) {
        if(controlMode.isDirect()){
            return false;
        }
        return ipFrameHandler.createJsonObject(dataTypeMask,transmitData);
    }

    public synchronized JSONObject getTransmitData() {
        if(controlMode.isDirect()){
            return null;
        }
        return ipFrameHandler.getTransmitData();
    }
}