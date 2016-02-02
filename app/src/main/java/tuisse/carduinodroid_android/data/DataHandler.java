package tuisse.carduinodroid_android.data;


import android.util.Log;

/**
 * Created by keX on 10.12.2015.
 */
public class DataHandler implements SerialFrameIF,IpFrameIF{
    private final String TAG = "CarduinoDataHandler";

    public CarduinoData data;
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
        //setControlMode(ControlMode.TRANSCEIVER);
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
            Log.d(TAG, "cm: " + cm + " controlMode: " + controlMode);
            if (controlMode == null || data == null) {
                if (cm.isDirect()) {
                    data = new CarduinoData();
                } else {
                    data = new CarduinoDroidData();
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
                                data = new CarduinoDroidData();
                                break;
                            default://DIRECT
                                if (data instanceof CarduinoDroidData) {
                                    data = new CarduinoData((CarduinoData) data);
                                }
                                break;
                        }
                        break;
                    }
                    case REMOTE: {
                        switch (cm) {
                            case TRANSCEIVER:
                                data = new CarduinoDroidData();
                                break;
                            case REMOTE:
                                //do nothing
                                break;
                            default://DIRECT
                                data = new CarduinoData();
                                break;
                        }
                        break;
                    }
                    default: {//Direct
                        switch (cm) {
                            case TRANSCEIVER:
                                    data = new CarduinoDroidData(data);
                                break;
                            case REMOTE:
                                data = new CarduinoDroidData();
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
            serialFrameHandler = new SerialFrameHandler(data);
            if(data instanceof CarduinoDroidData) {
                ipFrameHandler = new IpFrameHandler((CarduinoDroidData)data);
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

    public byte[] serialFrameAssembleTx() {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAssembleTx: " + controlMode.toString());
            return new byte[0];
        }
        else{
            return serialFrameHandler.serialFrameAssembleTx();
        }
    }

    public boolean serialFrameAppendRx(byte inChar) {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAppendRx: " + controlMode.toString());
            return false;
        }
        else{
            return serialFrameHandler.serialFrameAppendRx(inChar);
        }
    }
}