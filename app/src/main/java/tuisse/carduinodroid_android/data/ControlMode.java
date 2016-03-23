package tuisse.carduinodroid_android.data;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;
/**
 * <h1>Control Mode enum</h1>
 * Enum which codes the control mode of the android device
 *
 * @author Till Max Schwikal
 * @since 11.01.2016
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.data.DataHandler
 */
public enum ControlMode {
    /**
     * In case the device is in TRANSCEIVER mode, it is conntected to the Arduino mounted on a car
     * and a Remote PC or another android device in REMOTE mode.
     * It transmits the data to the Arduino device over serial, collects its own data and transmits
     * all data to the remote control device. A user cant actively steer a Carduinodroid with an
     * android device in TRANSCEIVER mode.
     */
    TRANSCEIVER(0),
    /**
     * In case the device is in REMOTE mode, it is connected only to another android device in
     * TRANSCEIVER mode. Its gets all data from the TRANSCEIVER mode device. The user steers and
     * monitors the device in REMOTE mode.
     */
    REMOTE(1),
    /**
     * In case the device is in DIRECT mode, it is connected only to the Arduino mounted on a car.
     * It transmitts the data to the Arduino device directly. The user steers and monitors the
     * device in DIRECT mode.
     */
    DIRECT(2);

    ControlMode(int m){
        mode = m;
    }
    private int mode;

    /**
     * conversion functions for control mode
     * @param x
     * @return control mode (ControlMode)
     */
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

    /**
     * conversion functions for control mode
     * @param cm
     * @return control mode (int)
     */
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

    /**
     * returns a displayable string of the mode
     * @return control mode string
     */
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

    /**
     * boolean function if the device is in a specific state
     * @return isTransceiver
     */
    public synchronized boolean isTransceiver(){
        return this == ControlMode.TRANSCEIVER;
    }
    /**
     * boolean function if the device is in a specific state
     * @return isRemote
     */
    public synchronized boolean isRemote(){
        return this == ControlMode.REMOTE;
    }
    /**
     * boolean function if the device is in a specific state
     * @return isDirect
     */
    public synchronized boolean isDirect(){
        return this == ControlMode.DIRECT;
    }
}
