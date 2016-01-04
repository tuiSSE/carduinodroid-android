package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public enum SerialType {
    NONE(0),BLUETOOTH(1),USB(2);
    SerialType(int t){
        type = t;
    }
    int type;
}
