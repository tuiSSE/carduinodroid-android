package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public enum CommunicationStatus {
    NONE(0), IP(1), SERIAL(2), BOTH(3), ERROR(-1), IP_ERROR(-2), SERIAL_ERROR(-3),BOTH_ERROR(-4);
    CommunicationStatus(int s){
        status = s;
    }
    private int status;
}
