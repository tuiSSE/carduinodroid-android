package tuisse.carduinodroid_android;

/**
 * Created by keX on 21.12.2015.
 */

public class ConnectionState {
    private ConnectionEnum state;
    private String error;

    ConnectionState(ConnectionEnum s) {
        state = s;
        if(isError()) {
            error = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateDefaultError);
        }
        else {
            error = "";
        }
    }
    ConnectionState(ConnectionEnum s,String e) {
        state = s;
        if(isError()) {
            error = e;
        }
        else {
            error = "";
        }
    }
    public synchronized ConnectionEnum getState(){
        return state;
    }

    public synchronized String getError(){
        return error;
    }

    public synchronized String getStateName(){
        String s = "";
        switch (state){
            case  IDLE:             s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateIdle);break;
            case  TRYFIND:          s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryFind);break;
            case  FOUND:            s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateFound);break;
            case  TRYCONNECT:       s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryConnect);break;
            case  CONNECTED:        s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateConnected);break;
            case  RUNNING:          s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateRunning);break;
            case  STREAMERROR:      s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateStreamError);break;
            case  TRYCONNECTERROR:  s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateTryConnectError);break;
            case  UNKNOWN:          s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateUnknown);break;
            default:                s = CarduinodroidApplication.getAppContext().getString(R.string.connectionStateError);break;
        }
        return s;
    }

    public synchronized boolean isIdle(){
        return state == ConnectionEnum.IDLE;
    }
    public synchronized boolean isTryFind(){
        return state == ConnectionEnum.TRYFIND;
    }
    public synchronized boolean isFound(){
        return state == ConnectionEnum.FOUND;
    }
    public synchronized boolean isTryConnect(){
        return state == ConnectionEnum.TRYCONNECT;
    }
    public synchronized boolean isConnected(){
        return state == ConnectionEnum.CONNECTED;
    }
    public synchronized boolean isRunning(){
        return state == ConnectionEnum.RUNNING;
    }
    public synchronized boolean isError(){
        return state == ConnectionEnum.ERROR || state == ConnectionEnum.STREAMERROR || state == ConnectionEnum.TRYCONNECTERROR;
    }
    public synchronized boolean isUnknown(){
        return state == ConnectionEnum.UNKNOWN;
    }
    public synchronized boolean isStarted(){
        return !(this.isUnknown() || this.isError() || this.isIdle());
    }
    public synchronized boolean isIdleError(){
        return this.isError() || this.isIdle();
    }
    public synchronized boolean isNotEqual(ConnectionState s){
        return this != s;
    }
}