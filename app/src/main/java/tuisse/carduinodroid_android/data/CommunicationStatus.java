package tuisse.carduinodroid_android.data;

/**
 * @AUTOR Till Max Schwikal
 * @DATE 04.01.2016
 *
 * Enumeration which codes the communication status of the application.
 *
 * The enumeration boolean functions which generalize the actual state.
 *
 * @param ILDE:
 *      No connection is tried to establish. IpService and SerialService are in idle state.
 * @param SERIAL_CONNECTING:
 *      Only the SerialService is trying to connect to a Carduino-Arduino-Board
 *      IpService is already running (ControlMode.Transceiver).
 * @param IP_CONNECTING:
 *      Only the IpService is trying to connect
 *          to a Transceiver Android in ControlMode.Remote or
 *          to a Remote Android/Desktop device in ControlMode.Transceiver
 *      SerialService is already running (ControlMode.Transceiver).
 * @param BOTH_CONNECTING:
 *      Both, IpService and SerialService are trying to connect to therir partners.
 * @param OK:
 *      The whole connection chain is established. The Carduinodroid is ready to be driven.
 * @param Error:
 *      A general error occured.
 * @param IP_ERROR:
 *      A error in IpService occured.
 *      SerialService state is not known.
 * @param SERIAL_ERROR:
 *      A error in SerialService occured.
 *      IpService state is not known.
 * @param BOTH_ERROR:
 *      IpService and SerialService got a error.
 *
 * @see ConnectionState
 * @see ControlMode
 * @see DataHandler
 * @see WatchdogService
 */

public enum CommunicationStatus {
    IDLE(0), SERIAL_CONNECTING(1), IP_CONNECTING(2), BOTH_CONNECTING(3), OK(4), ERROR(-1), IP_ERROR(-2), SERIAL_ERROR(-3), BOTH_ERROR(-4);
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