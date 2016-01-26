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
        switch(x) {
            case 1:
                return BLUETOOTH;
            case 2:
                return USB;
            case 3:
                return AUTO;
            default:
                return NONE;
        }
    }

    public static Integer toInteger(SerialType st){
        switch (st){
            case BLUETOOTH:
                return 1;
            case USB:
                return 2;
            case AUTO:
                return 3;
            default:
                return 0;
        }
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
