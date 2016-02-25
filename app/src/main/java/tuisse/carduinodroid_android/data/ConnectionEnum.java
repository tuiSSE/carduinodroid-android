package tuisse.carduinodroid_android.data;

/**
 * @author Till Max Schwikal
 * @date 12.01.2016
 *
 * Enumeration which abstractly codes the status of a connection service.
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
     * Constructor of ConnectionEnum
     *
     * @param s
     */
    ConnectionEnum(int s) {
        state = s;
    }
}
