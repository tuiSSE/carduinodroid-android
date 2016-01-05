package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public enum SerialType {
    NONE(0),BLUETOOTH(1),USB(2),AUTO(3);
    SerialType(int t){
        type = t;
    }
    int type;

    public static SerialType fromInteger(int x) {
        SerialType t;
        switch(x) {
            case 1:
                t=BLUETOOTH;
                break;
            case 2:
                t = USB;
                break;
            case 3:
                t=AUTO;
                break;
            default:
                t=NONE;
        }
        return t;
    }

    public boolean isAutoBluetooth(){
        return this == BLUETOOTH || this == AUTO;
    }

    public boolean isBluetooth(){
        return this == BLUETOOTH;
    }

    public boolean isAutoUsb(){
        return this == USB || this == AUTO;
    }

    public boolean isUsb(){
        return this == USB;
    }

    public boolean isAuto(){
        return this == AUTO;
    }
    public boolean isNone(){
        return this == NONE;
    }
}
