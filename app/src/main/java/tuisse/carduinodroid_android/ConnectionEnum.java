package tuisse.carduinodroid_android;

/**
 * Created by keX on 12.01.2016.
 */
public enum ConnectionEnum {

    IDLE(0),TRYFIND(1),FOUND(2), TRYCONNECT(3), CONNECTED(4), RUNNING(5), ERROR(-1), STREAMERROR(-2), TRYCONNECTERROR(-3),UNKNOWN(-4);
    private int state;
    ConnectionEnum(int s) {
        state = s;
    }
}
