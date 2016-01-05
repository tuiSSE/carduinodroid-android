package tuisse.carduinodroid_android;

/**
 * Created by keX on 07.12.2015.
 */
public class Preferences {
    private SerialType serialPref;
    public boolean rcNetwork1Activity0;

    public synchronized SerialType getSerialPref(){
        return serialPref;
    }
    public synchronized void setSerialPref(SerialType t){
        serialPref = t;
    }


}
