package tuisse.carduinodroid_android.data;

import android.util.Log;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;

/**
 * <h1>Connection State enum</h1>
 *
 * Class which abstractly codes the status of a connection service (ip or serial).
 *
 * @author Till Max Schwikal
 * @since 21.12.2015
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.data.ConnectionEnum
 */

public class ConnectionState {
    private final String TAG = "CarduinoConnectionState";

    /**
     * ConnectionEnum variable holds the actual state.
     */
    private ConnectionEnum state;

    /**
     * Error string which holds a description of the error.
     */
    private String error;

    /**
     * primitive constructor of the class
     * @param s new connection enum
     */
    public ConnectionState(ConnectionEnum s) {
        state = s;
        if(isError()) {
            error = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateDefaultError);
        }
        else {
            error = "";
        }
    }

    /**
     * Constructor with extra error message string.
     *
     * @param s new connection enum
     * @param e new error description
     */
    public ConnectionState(ConnectionEnum s, String e) {
        state = s;
        if(isError()) {
            error = e;
        }
        else {
            error = "";
        }
    }

    /**
     * Getter of the state
     *
     * @return current state
     */
    public synchronized ConnectionEnum getState(){
        return state;
    }

    /**
     * Getter of the error/description string
     * @return
     */
    public synchronized String getError(){
        return error;
    }

    /**
     * Getter of the state names for the UI.
     * @return state name
     */
    public synchronized String getStateName(){
        String s = "";
        switch (state){
            case IDLE:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateIdle);
                break;
            case TRYFIND:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryFind);
                break;
            case FOUND:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateFound);
                break;
            case TRYCONNECT:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryConnect);
                break;
            case CONNECTED:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateConnected);
                break;
            case RUNNING:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateRunning);
                break;
            case ERROR:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateError);
                break;
            case STREAMERROR:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateStreamError);
                break;
            case TRYCONNECTERROR:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryConnectError);
                break;
            case UNKNOWN:
                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateUnknown);
                break;
            default:
                s = "";
                Log.e(TAG, "getStateName: unknown connection state");
                break;
        }
        return s;
    }

    /**
     * Boolean state test function.
     * @return true if state is IDLE
     */
    public synchronized boolean isIdle(){
        return state == ConnectionEnum.IDLE;
    }

    /**
     * Boolean state test function.
     * @return true if state is TRYFIND
     */
    public synchronized boolean isTryFind(){
        return state == ConnectionEnum.TRYFIND;
    }

    /**
     * Boolean state test function.
     * @return true if state is FOUND
     */
    public synchronized boolean isFound(){
        return state == ConnectionEnum.FOUND;
    }

    /**
     * Boolean state test function.
     * @return true if state is TRYCONNECT
     */
    public synchronized boolean isTryConnect(){
        return state == ConnectionEnum.TRYCONNECT;
    }

    /**
     * Boolean state test function.
     * @return true if state is CONNECTED
     */
    public synchronized boolean isConnected(){
        return state == ConnectionEnum.CONNECTED;
    }

    /**
     * Boolean state test function.
     * @return true if state is RUNNING
     */
    public synchronized boolean isRunning(){
        return state == ConnectionEnum.RUNNING;
    }

    /**
     * Boolean state test function.
     * @return true if state is ERROR, STREAMERROR, TRYCONNECTERROR
     */
    public synchronized boolean isError(){
        return state == ConnectionEnum.ERROR || state == ConnectionEnum.STREAMERROR || state == ConnectionEnum.TRYCONNECTERROR;
    }

    /**
     * Boolean state test function.
     * @return true if state is UNKNOWN
     */
    public synchronized boolean isUnknown(){
        return state == ConnectionEnum.UNKNOWN;
    }

    /**
     * Boolean state test function.
     * @return true if state is TRYFIND, FOUND, TRYCONNECT, CONNECTED, RUNNING
     */
    public synchronized boolean isStarted(){
        return this.isTryFind() || this.isFound() || this.isTryConnect() || this.isConnected() || this.isRunning();
    }

    /**
     * Boolean state test function.
     * @return true if state is IDLE, ERROR, STREAMERROR, TRYCONNECTERROR
     */
    public synchronized boolean isIdleError(){
        return this.isError() || this.isIdle();
    }
}