package tuisse.carduinodroid_android.data;


import android.graphics.drawable.LayerDrawable;
import android.util.Log;

/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer implements SerialTx, SerialRx, Serial, Ip{
    public Preferences preferences;
    protected SerialData serialData;
    protected IpData ipData;

    private boolean bluetoothEnabled;

    public DataContainer() {
        serialData = new SerialData();
        ipData = new IpData();
        preferences = new Preferences();
        setBluetoothEnabled(false);
    }

    public synchronized void setBluetoothEnabled(boolean bte){
        bluetoothEnabled = bte;
    }
    public synchronized boolean getBluetoothEnabled(){
        return bluetoothEnabled;
    }

    public synchronized CommunicationStatus getCommunicationStatus() {
        //TODO: implement
        return CommunicationStatus.NONE;
    }

    public synchronized byte getSpeed() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getSpeed();
        }
    }

    public synchronized byte getSteer() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getSteer();
        }
    }

    public synchronized int getStatusLed() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getStatusLed();
        }
    }

    public synchronized int getFrontLight() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getFrontLight();
        }
    }

    public synchronized int getResetAccCur() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getResetAccCur();
        }
    }

    public synchronized int getFailSafeStop() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialTx.getFailSafeStop();
        }
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
            default:// TRANSCEIVER
                //do nothing
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
            default:// TRANSCEIVER
                //do nothing
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
            default:// TRANSCEIVER
                //do nothing
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
            default:// TRANSCEIVER
                //do nothing
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
                //do nothinng
                break;
        }
    }

    public void resetMotors() {
        if(preferences.getControlMode().isRemote()) {
            //// TODO: 26.01.2016 implement
        }
        else{//DIRECT + TRANSCEIVER
            serialData.serialTx.resetMotors();
        }
    }

    public byte[] serialGet() {
        if(preferences.getControlMode().isRemote()) {
            Log.e("Carduino", "wrong control Mode: " + preferences.getControlMode().toString());
            return new byte[0];
        }
        else {//DIRECT + TRANSCEIVER
            return serialData.serialTx.serialGet();
        }
    }

    public float getCurrent() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getCurrent();
        }
    }

    public synchronized float getAbsoluteBatteryCapacity() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getAbsoluteBatteryCapacity();
        }
    }

    public synchronized float getPercentBatteryCapacity() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getPercentBatteryCapacity();
        }
    }

    public synchronized float getVoltage() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getVoltage();
        }
    }

    public synchronized float getDs2745Temperature() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getDs2745Temperature();
        }
    }

    public synchronized float getUltrasoundFront() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getUltrasoundFront();
        }
    }

    public synchronized float getUltrasoundBack() {
        if(preferences.getControlMode().isRemote()) {
            //TODO implement
            return 0.0f;
        }
        else{//DIRECT + TRANSCEIVER
            return serialData.serialRx.getUltrasoundBack();
        }
    }

    public boolean serialAppend(byte inChar) {
        switch (preferences.getControlMode()) {
            case REMOTE:
                Log.e("Carduino","wrong control Mode: " + preferences.getControlMode().toString());
                return false;
            default://DIRECT + TRANSCEIVER
                return serialData.serialRx.serialAppend(inChar);
        }
    }

    public synchronized void setSerialState(ConnectionState state) {
        serialData.setSerialState(state);
    }

    public synchronized void setSerialName(String s) {
        serialData.setSerialName(s);
    }

    public synchronized void setSerialType(SerialType type) {
        serialData.setSerialType(type);
    }

    public synchronized ConnectionState getSerialState() {
        return serialData.getSerialState();
    }

    public synchronized String getSerialName() {
        return serialData.getSerialName();
    }

    public synchronized SerialType getSerialType() {
        return serialData.getSerialType();
    }

    public LayerDrawable getSerialConnLogoId(SerialType serialPref) {
        return serialData.getSerialConnLogoId(serialPref);
    }

    public LayerDrawable getIpConnLogoId() {
        return ipData.getIpConnLogoId();
    }

    public String getRemoteIp() {
        return ipData.getRemoteIp();
    }

    public String getTransceiverIp() {
        return ipData.getTransceiverIp();
    }

    public ConnectionState getIpState() {
        return ipData.getIpState();
    }

    public IpType getIpType() {
        return ipData.getIpType();
    }

    public void setIpState(ConnectionState is) {
        ipData.setIpState(is);
    }

    public void setIpType(IpType it) {
        ipData.setIpType(it);
    }
}