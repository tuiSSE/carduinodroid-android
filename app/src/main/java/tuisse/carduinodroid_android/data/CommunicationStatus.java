package tuisse.carduinodroid_android.data;

/**
 * @author Till Max Schwikal
 * @date 04.01.2016
 *
 * Enumeration which codes the communication status of the application.
 *
 * @see ConnectionState
 * @see ControlMode
 * @see DataHandler
 */

public enum CommunicationStatus {
    /// Group: Idle
    /**
     * No connection is tried to establish. IpService and SerialService are in idle state.
     */
    IDLE(0),

    /// Group: Connecting
    /**
     * Only the SerialService is trying to connect to a
     * Carduino-Arduino-Board IpService is already running (ControlMode.Transceiver).
     */
    SERIAL_CONNECTING(1),
    /**
     * Only the IpService is trying to connect
     * to a Transceiver Android in ControlMode.Remote or
     * to a Remote Android/Desktop device in ControlMode.Transceiver
     * SerialService is already running (ControlMode.Transceiver).
     */
    IP_CONNECTING(2),
    /**
     * Both, IpService and SerialService are trying to connect to therir partners.
     */
    BOTH_CONNECTING(3),

    /// Group: Ok
    /**
     * The whole connection chain is established. The Carduinodroid is ready to be driven.
     */
    OK(4),

    /// Group: Error
    /**
     * A general error occured.
     */
    ERROR(-1),
    /**
     * A error in IpService occured.
     * SerialService state is not known.
     */
    IP_ERROR(-2),
    /**
     * A error in SerialService occured.
     * IpService state is not known.
     */
    SERIAL_ERROR(-3),
    /**
     * IpService and SerialService got a error.
     */
    BOTH_ERROR(-4);

    /**
     * constructor for CommunicationStatus
     * @param s
     */
    CommunicationStatus(int s){
        status = s;
    }
    private int status;

    /**
     * Boolean check function for OK state.
     * @return boolean
     */
    public synchronized boolean isOk(){
        return this == CommunicationStatus.OK;
    }

    /**
     * Boolean check function for connecting states.
     * @return boolean
     */
    public synchronized boolean isConnecting(){
        return  (this == CommunicationStatus.SERIAL_CONNECTING) ||
                (this == CommunicationStatus.IP_CONNECTING) ||
                (this == CommunicationStatus.BOTH_CONNECTING);
    }

    /**
     * Boolean check function for error states.
     * @return boolean
     */
    public synchronized boolean isError(){
        return  (this == CommunicationStatus.ERROR) ||
                (this == CommunicationStatus.IP_ERROR) ||
                (this == CommunicationStatus.SERIAL_ERROR) ||
                (this == CommunicationStatus.BOTH_ERROR);
    }

    /**
     * Boolean check function for IDLE state.
     * @return boolean
     */
    public synchronized boolean isIdle(){
        return this == CommunicationStatus.IDLE;
    }


    /**
     * Boolean check function for IDLE or isError states.
     * @return boolean
     */
    public synchronized boolean isIdleError(){
        return isIdle() || isError();
    }
}