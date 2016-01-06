package tuisse.carduinodroid_android;

/**
 * Created by keX on 21.12.2015.
 */

public enum ConnectionState {
    IDLE(0),TRYFIND(1),FOUND(2), TRYCONNECT(3), CONNECTED(4), RUNNING(5), ERROR(-1), STREAMERROR(-2);
    ConnectionState(int s) {
        state = s;
    }
    private int state;

    public static ConnectionState fromInteger(int x) {
        ConnectionState s;
        switch(x) {
            case 0:
                s = IDLE;
                break;
            case 1:
                s = TRYFIND;
                break;
            case 2:
                s = FOUND;
                break;
            case 3:
                s = TRYCONNECT;
                break;
            case 4:
                s = CONNECTED;
                break;
            case 5:
                s = RUNNING;
                break;
            case -1:
                s = ERROR;
                break;
            case -2:
                s = STREAMERROR;
                break;
            default:
                s = ERROR;
        }
        return s;
    }

    public synchronized String getStateName(){
        String s = "";
        switch (state){
            case  0: s = "Idle";break;
            case  1: s = "Try to find";break;
            case  2: s = "Found";break;
            case  3: s = "Try to connect ...";break;
            case  4: s = "Connected";break;
            case  5: s = "Running";break;
            case -2: s = "Streamerror";break;
            default: s = "Error";break;
        }
        return s;
    }

    public synchronized boolean isIdle(){
        return this == IDLE;
    }
    public synchronized boolean isTryFind(){
        return this == TRYFIND;
    }
    public synchronized boolean isFound(){
        return this == FOUND;
    }
    public synchronized boolean isTryConnect(){
        return this == TRYCONNECT;
    }
    public synchronized boolean isConnected(){
        return this == CONNECTED;
    }
    public synchronized boolean isRunning(){
        return this == RUNNING;
    }
    public synchronized boolean isError(){
        return this == ERROR || this == STREAMERROR;
    }
    public synchronized boolean isIdleError(){
        return this.isError() || this.isIdle();
    }
    public synchronized boolean isNotEqual(ConnectionState s){
        return this != s;
    }
}