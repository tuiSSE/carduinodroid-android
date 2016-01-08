package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public class SerialData {
    public SerialProtocolRx serialRx;
    public SerialProtocolTx serialTx;

    private ConnectionState connectionState;
    private SerialType serialType;
    private String serialName;

    public SerialData(){
        serialRx = new SerialProtocolRx();
        serialTx = new SerialProtocolTx();

        setConnectionState(ConnectionState.IDLE);
        setSerialName("");
        setSerialType(SerialType.NONE);
    }

    public synchronized void setConnectionState(ConnectionState state){
        connectionState = state;
    }

    public synchronized ConnectionState getConnectionState(){
        return connectionState;
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

    public synchronized int getSerialConnLogoId(){
        int logo;
        switch (connectionState){
            case  TRYFIND:
            case  FOUND:
            case  TRYCONNECT:
                logo = R.drawable.status_try_connect;
                break;
            case  CONNECTED:
            case  RUNNING:
                logo = R.drawable.status_connected;
                break;
            case ERROR:
                logo = R.drawable.status_error;
                break;
            case STREAMERROR:
                logo = R.drawable.status_connected_error;
                break;
            case TRYCONNECTERROR:
                logo = R.drawable.status_try_connect_error;
                break;
            default:
                logo = R.drawable.status_idle;
                break;
        }
        return logo;
    }

    public synchronized int getSerialTypeLogoId(SerialType pref){
        int logo;
        switch (serialType){
            case NONE:
                logo = R.drawable.serial_type_none;
                break;
            case USB:
                if(pref.isAuto()){
                    logo = R.drawable.serial_type_auto_usb;
                }
                else{
                    logo = R.drawable.serial_type_usb;
                }
                break;
            case BLUETOOTH:
                if(pref.isAuto()){
                    logo = R.drawable.serial_type_auto_bt;
                }
                else{
                    logo = R.drawable.serial_type_bt;
                }
                break;
            default:
                logo = R.drawable.serial_type_none;
                break;
        }
        return logo;
    }
}