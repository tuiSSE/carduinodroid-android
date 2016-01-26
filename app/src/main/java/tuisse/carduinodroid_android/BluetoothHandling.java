package tuisse.carduinodroid_android;

/**
 * Created by mate on 26.01.2016.
 */
public enum BluetoothHandling {
    AUTO(0),OFF(1),ON(2);
    int handling;
    BluetoothHandling(int bth){
        handling = bth;
    }

    public static BluetoothHandling fromInteger(int x) {
        switch(x) {
            case 1:
                return OFF;
            case 2:
                return ON;
            default:
                return AUTO;
        }
    }

    public static Integer toInteger(BluetoothHandling bth){
        switch (bth){
            case OFF:
                return 1;
            case ON:
                return 2;
            default:
                return 3;
        }
    }
}
