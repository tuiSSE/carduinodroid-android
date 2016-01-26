package tuisse.carduinodroid_android;


/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer {
    protected Preferences preferences;
    protected SerialData serialData;
    protected IpData ipData;

    public DataContainer() {
        serialData = new SerialData();
        ipData = new IpData();
        preferences = new Preferences();
    }

    public synchronized CommunicationStatus getCommunicationStatus() {
        //TODO: implement
        return CommunicationStatus.NONE;
    }

    public synchronized void setSpeed(int s) {
        switch (preferences.getControlMode()) {
            case DIRECT:
                serialData.serialTx.setSpeed(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }

    public synchronized void setSteer(int s) {
        switch (preferences.getControlMode()) {
            case DIRECT:
                serialData.serialTx.setSteer(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }

    public synchronized void setStatusLed(int s) {
        switch (preferences.getControlMode()) {
            case DIRECT:
                serialData.serialTx.setStatusLed(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }

    public synchronized void setFrontLight(int s) {
        switch (preferences.getControlMode()) {
            case DIRECT:
                serialData.serialTx.setFrontLight(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }

    public synchronized void setFailSafeStop(int s) {
        switch (preferences.getControlMode()) {
            case DIRECT:
                serialData.serialTx.setFailSafeStop(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }

    public synchronized void setResetAccCur(int s){
        switch(preferences.getControlMode()){
            case DIRECT:
                serialData.serialTx.setResetAccCur(s);
                break;
            case REMOTE:
                //// TODO: 26.01.2016 implement
                break;
            default:
                break;
        }
    }
}