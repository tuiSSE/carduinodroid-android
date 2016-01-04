package tuisse.carduinodroid_android;

/**
 * Created by keX on 21.12.2015.
 */

public enum ConnectionState {
    IDLE(0), TRYCONNECT(1), CONNECTED(1), RUNNING(3), ERROR(-1), STREAMERROR(-2);
    ConnectionState(int s) {
        state = s;
    }
    private int state;

    public String getStateName(){
        String s = "";
        switch (state){
            case  0: s = "Idle";break;
            case  1: s = "Try to connect ...";break;
            case  2: s = "Connected";break;
            case  3: s = "Running";break;
            case -2: s = "Streamerror";break;
            default: s = "Error";break;
        }
        return s;
    }
}