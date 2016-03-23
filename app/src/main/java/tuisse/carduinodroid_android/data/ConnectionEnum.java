package tuisse.carduinodroid_android.data;

import android.util.Log;

/**
 * <h1>Connection Enum</h1>
 * Enumeration which abstractly codes the status of a connection service.
 *
 * @author Till Max Schwikal
 * @since 12.01.2016
 * @version 1.0
 */
public enum ConnectionEnum {

    /// Group: Idle
    /**
     * No connection is tried to establish.
     */
    IDLE(0),

    /// Group: establishing connection
    /**
     * A communication partner is tried to find.
     */
    TRYFIND(1),
    /**
     * A communication partner is found.
     */
    FOUND(2),
    /**
     * A connection with is tried to establish.
     */
    TRYCONNECT(3),
    /**
     * A connection is established.
     */
    CONNECTED(4),

    /// Group: Running
    /**
     * Thread to send and receive data is running.
     */
    RUNNING(5),

    /// Group: Error
    /**
     * A Error occured.
     */
    ERROR(-1),
    /**
     * A streamerror occured.
     */
    STREAMERROR(-2),
    /**
     * A Error while connection establishment occured
     */
    TRYCONNECTERROR(-3),

    /// Group: third party state
    /**
     * State is currently not known by this device.
     */
    UNKNOWN(-4);

    /**
     * private intern connection state.
     */
    private int state;

    /**
     * private TAG for Log
     */
    private static String TAG = "CarduinoConnectionEnum";

    /**
     * Constructor of ConnectionEnum
     *
     * @param s
     */
    ConnectionEnum(int s) {
        state = s;
    }

    /**
     * @brief conversion Function Integer to ConnectionEnum
     *
     * @return always a valid ConnectionEnum
     */
    public static ConnectionEnum fromInteger(int x) {
        switch(x) {
            case 0:
                return IDLE;
            case 1:
                return TRYFIND;
            case 2:
                return FOUND;
            case 3:
                return TRYCONNECT;
            case 4:
                return CONNECTED;
            case 5:
                return RUNNING;
            case -1:
                return ERROR;
            case -2:
                return STREAMERROR;
            case -3:
                return TRYCONNECTERROR;
            case -4:
                return UNKNOWN;
            default:
                Log.e(TAG, "no valid conversion. Took IDLE");
                return IDLE;
        }
    }

    /**
     * @brief conversion Function ConnectionEnum to Integer
     *
     * @return always a valid ConnectionEnum-Integer
     */
    public static Integer toInteger(ConnectionEnum ce){
        switch (ce){
            case IDLE:
                return 0;
            case TRYFIND:
                return 1;
            case FOUND:
                return 2;
            case TRYCONNECT:
                return 3;
            case CONNECTED:
                return 4;
            case RUNNING:
                return 5;
            case ERROR:
                return -1;
            case STREAMERROR:
                return -2;
            case TRYCONNECTERROR:
                return -3;
            case UNKNOWN:
                return -4;
            default:
                Log.e(TAG, "no valid conversion. Took AUTO");
                return 0;
        }
    }
}
