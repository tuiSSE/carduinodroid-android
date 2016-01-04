package tuisse.carduinodroid_android;

import android.app.Application;

/**
 * Created by keX on 04.01.2016.
 */
public class SerialData {
    public SerialProtocolRx serialRx;
    public SerialProtocolTx serialTx;

    private SerialState serialState;
    private SerialType serialType;
    private String serialName;

    public SerialData(){

        serialRx = new SerialProtocolRx();
        serialTx = new SerialProtocolTx();

        serialState = SerialState.IDLE;
        serialName = "";
        serialType = SerialType.NONE;
    }

    public synchronized void setSerialState(SerialState state){
        serialState = state;
    }

    public synchronized SerialState getSerialState(){
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

    public synchronized int getLogoId(){
        int logo;
        switch (serialState){
            case  TRYCONNECT:
                if(serialType == SerialType.BLUETOOTH)
                    logo = R.drawable.serial_bt_try_connect;
                else
                    logo = R.drawable.serial_usb_try_connect;
                break;
            case  RUNNING:
            case  CONNECTED:
                if(serialType == SerialType.BLUETOOTH)
                    logo = R.drawable.serial_bt_connected;
                else
                    logo = R.drawable.serial_usb_connected;
                break;
            case ERROR:
            case STREAMERROR:
                logo = R.drawable.serial_error;
                break;
            default:
                logo = R.drawable.serial_idle;
                break;
        }
        return logo;
    }
}