package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class Preferences {
    private SerialType serialPref = SerialType.NONE;
    public boolean rcNetwork1Activity0 = true;
    private String bluetoothDeviceName = " ";
    private ControlMode controlMode = ControlMode.TRANSCEIVER;

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
    public synchronized void setControlModeNext(){
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
    }
    public synchronized void setControlModePrev(){
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
    }
}
