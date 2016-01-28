package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class Preferences {
    private SerialType serialPref = SerialType.NONE;
    private String bluetoothDeviceName = "";
    private BluetoothHandling bluetoothHandling = BluetoothHandling.AUTO;
    private ControlMode controlMode = ControlMode.TRANSCEIVER;
    private boolean failSafeStopPref = true;
    private boolean debugView = true;

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

    public synchronized void setControlMode(ControlMode cm){
        controlMode = cm;
    }
    public synchronized ControlMode getControlMode(){
        return controlMode;
    }

    public synchronized int setControlModeNext(){
        switch (controlMode){
            case TRANSCEIVER:
                controlMode = ControlMode.REMOTE;
                break;
            case REMOTE:
                controlMode = ControlMode.DIRECT;
                break;
            case DIRECT:
                controlMode = ControlMode.TRANSCEIVER;
                break;
            default:
                controlMode = ControlMode.TRANSCEIVER;
                break;
        }
        return ControlMode.toInteger(controlMode);
    }
    public synchronized int setControlModePrev(){
        switch (controlMode){
            case TRANSCEIVER:
                controlMode = ControlMode.DIRECT;
                break;
            case REMOTE:
                controlMode = ControlMode.TRANSCEIVER;
                break;
            case DIRECT:
                controlMode = ControlMode.REMOTE;
                break;
            default:
                controlMode = ControlMode.TRANSCEIVER;
                break;
        }
        return ControlMode.toInteger(controlMode);
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
}
