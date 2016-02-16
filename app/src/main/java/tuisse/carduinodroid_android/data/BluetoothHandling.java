package tuisse.carduinodroid_android.data;

import android.util.Log;

/**
 * @AUTOR Till Max Schwikal
 * @DATE 26.01.2016
 *
 * @enum Codes the bluetooth handling after establishing a connection.
 *
 * @brief The enumeration has static conversion functions fromInteger and toInteger.
 *
 * @see DataHandler
 */
public enum BluetoothHandling {

    AUTO(0),///< turn bluetooth to the same state as chosen before.
    OFF(1), ///< turn bluetooth off. This case is independent of the previous bluetooth state.
    ON(2);  ///< turn bluetooth on. This case is independent of the previous bluetooth state.

    private int handling;
    private static String TAG = "CarduinoBluetoothHandling";
    BluetoothHandling(int bth){
        handling = bth;
    }

    /**
     * @brief conversion Function Integer to BluetoothHandling
     *
     * @return always a valid BluetoothHandling
     */
    public static BluetoothHandling fromInteger(int x) {
        switch(x) {
            case 0:
                return AUTO;
            case 1:
                return OFF;
            case 2:
                return ON;
            default:
                Log.e(TAG, "no valid conversion. Took AUTO");
                return AUTO;
        }
    }

    /**
     * @brief conversion Function BluetoothHandling to Integer
     *
     * @return always a valid BluetoothHandling-Integer
     */
    public static Integer toInteger(BluetoothHandling bth){
        switch (bth){
            case AUTO:
                return 0;
            case OFF:
                return 1;
            case ON:
                return 2;
            default:
                Log.e(TAG, "no valid conversion. Took AUTO");
                return 0;
        }
    }
}