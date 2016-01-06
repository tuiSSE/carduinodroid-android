package tuisse.carduinodroid_android;

/**
 * Created by keX on 07.12.2015.
 */
public class Preferences {
    private SerialType serialPref = SerialType.NONE;
    public boolean rcNetwork1Activity0 = true;
    private String bluetoothDeviceName = " ";

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

}
