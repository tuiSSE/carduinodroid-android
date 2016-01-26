package tuisse.carduinodroid_android;

import android.graphics.drawable.LayerDrawable;
import android.util.Log;

/**
 * Created by keX on 04.01.2016.
 */
public class SerialData {
    private final String TAG = "CarduinoSerialData";

    public SerialProtocolRx serialRx;
    public SerialProtocolTx serialTx;

    private ConnectionState serialState;
    private SerialType serialType;
    private String serialName;

    private boolean bluetoothEnabled;

    public SerialData(){
        serialRx = new SerialProtocolRx();
        serialTx = new SerialProtocolTx();

        serialState = new ConnectionState(ConnectionEnum.IDLE,"");
        setSerialName(CarduinodroidApplication.getAppContext().getString(R.string.serialDeviceNone));
        setSerialType(SerialType.NONE);
        setBluetoothEnabled(false);
    }

    public synchronized void setBluetoothEnabled(boolean bte){
        bluetoothEnabled = bte;
    }
    public synchronized boolean getBluetoothEnabled(){
        return bluetoothEnabled;
    }

    public synchronized void setSerialState(ConnectionState state){
        serialState = state;
    }

    public synchronized ConnectionState getSerialState(){
        return serialState;
    }

    public synchronized void setSerialType(SerialType type){
        serialType = type;
    }

    public synchronized SerialType getSerialType(){
        return serialType;
    }

    public synchronized void setSerialName(String s){
        serialName = s;
    }

    public synchronized String getSerialName(){
        return serialName;
    }

    public synchronized LayerDrawable getSerialConnLogoId(SerialType serialPref){
        int state;
        int type;
        switch (serialState.getState()){
            case  TRYFIND:
            case  FOUND:
            case  TRYCONNECT:
                state = R.drawable.status_try_connect;
                break;
            case  CONNECTED:
            case  RUNNING:
                state = R.drawable.status_connected;
                break;
            case ERROR:
                state = R.drawable.status_error;
                break;
            case STREAMERROR:
                state = R.drawable.status_connected_error;
                break;
            case TRYCONNECTERROR:
                state = R.drawable.status_try_connect_error;
                break;
            case UNKNOWN:
                state = R.drawable.status_unknown;
                break;
            default:
                state = R.drawable.status_idle;
                break;
        }
        if(serialState.isUnknown()){
            type = R.drawable.serial_type_none;
        }
        else{
            switch (serialType){
                case BLUETOOTH:
                    if(serialPref.isAuto())
                        type = R.drawable.serial_type_auto_bt;
                    else
                        type = R.drawable.serial_type_bt;
                    break;
                case USB:
                    if(serialPref.isAuto())
                        type = R.drawable.serial_type_auto_usb;
                    else
                        type = R.drawable.serial_type_usb;
                    break;
                case AUTO:
                    //should never happen
                    Log.e(TAG, "serialType is Auto");
                    type = R.drawable.serial_type_none;
                    break;
                default://NONE
                    switch (serialPref){
                        case USB:
                            type = R.drawable.serial_type_none_usb;
                            break;
                        case BLUETOOTH:
                            type = R.drawable.serial_type_none_bt;
                            break;
                        case AUTO:
                            type = R.drawable.serial_type_none_auto;
                            break;
                        default://NONE
                            //should never happen
                            Log.e(TAG, "serialType is NONE, serialPref is NONE");
                            type = R.drawable.serial_type_none;
                            break;
                    }
            }
        }
        return Utils.assembleDrawables(state,type);
    }
}