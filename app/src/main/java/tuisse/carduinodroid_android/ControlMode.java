package tuisse.carduinodroid_android;

/**
 * Created by keX on 11.01.2016.
 */
public enum ControlMode {
    TRANSCEIVER(0),REMOTE(1),DIRECT(2);
    ControlMode(int m){
        mode = m;
    }
    private int mode;

    public static ControlMode fromInteger(int x) {
        ControlMode m;
        switch(x) {
            case 0:
                m = TRANSCEIVER;
                break;
            case 1:
                m = REMOTE;
                break;
            case 2:
                m = DIRECT;
                break;
            default:
                m = TRANSCEIVER;
        }
        return m;
    }
    public static int toInteger(ControlMode cm){
        int x;
        switch (cm){
            case TRANSCEIVER:
                return 0;
            case REMOTE:
                return 1;
            case DIRECT:
                return 2;
            default:
                return 0;
        }
    }

    public synchronized String getString(){
        String s;
        switch (mode) {
            case 0:
                s = CarduinodroidApplication.getAppContext().getString(R.string.controlModeTransceiver);
                break;
            case 1:
                s = CarduinodroidApplication.getAppContext().getString(R.string.controlModeRC);
                break;
            case 2:
                s = CarduinodroidApplication.getAppContext().getString(R.string.controlModeDirect);
                break;
            default:
                s = CarduinodroidApplication.getAppContext().getString(R.string.controlModeTransceiver);
        }
        return s;
    }

    public synchronized boolean isTransceiver(){
        return this == ControlMode.TRANSCEIVER;
    }
    public synchronized boolean isRemote(){
        return this == ControlMode.REMOTE;
    }
    public synchronized boolean isDirect(){
        return this == ControlMode.DIRECT;
    }
}
