package tuisse.carduinodroid_android.data;

/**
 * @AUTOR Till Max Schwikal
 * @DATE 12.01.2016
 *
 * Enumeration which abstractly codes the status of a connection service.
 *
 * The state are a global definition of the service states. They can be devided in four groups:
 * service is idle (IDLE)
 * service establishes connection (TRYFIND, FOUND, TRYCONNECT, CONNECTED)
 * service is running (RUNNING)
 * service has a error (ERROR, STREAMERROR, TRYCONNECTERROR, UNKNOWN)
 *
 * @param ILDE:
 *      No connection is tried to establish. IpService and SerialService are in idle state.
 */
public enum ConnectionEnum {

    IDLE(0),
    TRYFIND(1), FOUND(2), TRYCONNECT(3), CONNECTED(4),
    RUNNING(5),
    ERROR(-1), STREAMERROR(-2), TRYCONNECTERROR(-3),UNKNOWN(-4);
    private int state;
    ConnectionEnum(int s) {
        state = s;
    }
}
