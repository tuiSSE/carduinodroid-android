package tuisse.carduinodroid_android.data;

/**
 * Created by keX on 04.01.2016.
 */
public enum CommunicationStatus {
    IDLE(0), SERIAL_CONNECTING(1), IP_CONNECTING(2), BOTH_CONNECTING(3), OK(4), ERROR(-1), IP_ERROR(-2), SERIAL_ERROR(-3), BOTH_ERROR(-4);
    CommunicationStatus(int s){
        status = s;
    }
    private int status;
}
