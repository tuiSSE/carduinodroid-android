package tuisse.carduinodroid_android.data;

/**
 * @AUTOR Till Max Schwikal
 * @DATE 26.01.2016
 *
 * Enumeration which codes the bluetooth handling after establishing a connection.
 *
 * The enumeration has static conversion functions fromInteger and toInteger.
 *
 * @param OFF:  turn bluetooth off. This case is independent of the previous bluetooth state.
 * @param ON:   turn bluetooth on. This case is independent of the previous bluetooth state.
 * @param AUTO: turn bluetooth to the same state as chosen before.
 *
 * @see #DataHandler.setBluetoothHandling(int)
 * @see #DataHandler.getBluetoothHandling
 */
public enum BluetoothHandling {
    AUTO(0),OFF(1),ON(2);
    int handling;
    BluetoothHandling(int bth){
        handling = bth;
    }

    public static BluetoothHandling fromInteger(int x) {
        switch(x) {
            case 0:
                return AUTO;
            case 1:
                return OFF;
            case 2:
                return ON;
            default:
                throw Exception e;
        }
    }

    public static Integer toInteger(BluetoothHandling bth){
        switch (bth){
            case AUTO:
                return 0;
            case OFF:
                return 1;
            case ON:
                return 2;
            default:
                throw Exception e;
        }
    }
}