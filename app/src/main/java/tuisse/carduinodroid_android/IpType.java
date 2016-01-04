package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public enum IpType {
    NONE(0),IP(1),SERIAL_ONLY(2);
    IpType(int t){
        type = t;
    }
    int type;
}
