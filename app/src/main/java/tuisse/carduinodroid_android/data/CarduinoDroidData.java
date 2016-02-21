package tuisse.carduinodroid_android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.Constants;

import tuisse.carduinodroid_android.R;

/**
 * Created by mate on 02.02.2016.
 */
public class CarduinoDroidData implements CarduinoDroidIF{
    private final String TAG = "CarduinoDroidData";

    private ConnectionState ipState;
    private IpType ipType;

    private String cameraSupportedSizes; // All the possible Resolutions/Sizes supported by the Transceiver
    private int cameraType; // Front= - Back=
    private int cameraResolution; // Defines position of the resolution out of an String-Array with all supported ones
    private int cameraFlashlight; // On=1 - Off=0;
    private int cameraQuality; // Value between 0(low) to 100 (high)

    private int soundPlay; // Play a horn sound=1 - No horn sound=0;
    private int soundRecord; // Start Recording=1 - Stop Recording=0;

    private int gpsData;//
    private int vibration;//

    private int mobileAvail;//
    private int mobileActive;//
    private int wlanAvail;//
    private int wlanActive;//

    private String remoteIp;
    private String transceiverIp;
    private String myIp;

    private byte[] cameraPicture;

    public CarduinoDroidData(){
        super();
        setIpState(new ConnectionState(ConnectionEnum.IDLE));
        setIpType(IpType.WLAN);

        setTransceiverIp("");
        setRemoteIp("");
        setMyIp("");

        setCameraType(0);
        setCameraResolution(0);
        setCameraFlashlight(0);
        setCameraQuality(100);

        setSoundPlay(0);
        setSoundRecord(0);

        setGpsData(0);
        setVibration(0);

        setMobileActive(0);
        setMobileAvailable(0);

        setWlanActive(0);
        setWlanAvailable(0);
    }

    public synchronized LayerDrawable getIpConnLogoId(){
        int status;
        int type;
        switch (ipState.getState()){
            case  TRYFIND:
            case  FOUND:
            case  TRYCONNECT:
                status = R.drawable.status_try_connect;
                break;
            case  CONNECTED:
            case  RUNNING:
                status = R.drawable.status_connected;
                break;
            case ERROR:
                status = R.drawable.status_error;
                break;
            case STREAMERROR:
                status = R.drawable.status_connected_error;
                break;
            case TRYCONNECTERROR:
                status = R.drawable.status_try_connect_error;
                break;
            case UNKNOWN:
                status = R.drawable.status_unknown;
                break;
            default:
                status = R.drawable.status_idle;
                break;
        }
        if(ipState.isUnknown()){
            type = R.drawable.ip_type_none;
        }
        else{
            switch (ipType){
                case WLAN:
                    type = R.drawable.ip_type_wlan;
                    break;
                default:
                    type = R.drawable.ip_type_none;
                    break;
            }
        }
        return Utils.assembleDrawables(status, type);
    }

    public synchronized String getRemoteIp(){
        return remoteIp;
    }

    public synchronized void setRemoteIp(String _remoteIp){
        remoteIp = _remoteIp;
    }

    public synchronized String getTransceiverIp(){
        return transceiverIp;
    }

    public synchronized void setTransceiverIp(String _transceiverIp){
        transceiverIp = _transceiverIp;
    }

    public synchronized String getMyIp(){
        return myIp;
    }

    public synchronized void setMyIp(String _myIp){
        myIp = _myIp;
    }

    public synchronized ConnectionState getIpState(){
        return ipState;
    }
    public synchronized void setIpState(ConnectionState is){
        ipState = is;
    }
    public synchronized IpType getIpType(){
        return ipType;
    }
    public synchronized void setIpType(IpType it){
        ipType = it;
    }

    public synchronized String getCameraSupportedSizes() { return cameraSupportedSizes;}
    public synchronized int getCameraType(){
        return cameraType;
    }
    public synchronized int getCameraResolution(){
        return cameraResolution;
    }
    public synchronized int getCameraFlashlight(){
        return cameraFlashlight;
    }
    public synchronized int getCameraQuality(){
        return cameraQuality;
    }

    public synchronized int getSoundPlay(){
        return soundPlay;
    }
    public synchronized int getSoundRecord(){
        return soundRecord;
    }

    public synchronized void setCameraSupportedSizes(String _cameraSupportedSizes) { cameraSupportedSizes = _cameraSupportedSizes;}
    public synchronized void setCameraType(int _cameraType) {
        cameraType = _cameraType;
    }
    public synchronized void setCameraResolution(int _cameraResolution) {
        cameraResolution = _cameraResolution;
    }
    public synchronized void setCameraFlashlight(int _cameraFlashlight) {
        cameraFlashlight = _cameraFlashlight;
    }
    public synchronized void setCameraQuality(int _cameraQuality) {
        cameraQuality = _cameraQuality;
    }

    public synchronized void setSoundPlay(int _soundPlay) {
        soundPlay = _soundPlay;
    }
    public synchronized void setSoundRecord(int _soundRecord) {
        soundRecord = _soundRecord;
    }

    public synchronized void setMobileAvailable(int _mobileAvailable) {
        mobileAvail = _mobileAvailable;
    }
    public synchronized void setMobileActive(int _mobileActive) {
        mobileActive = _mobileActive;
    }
    public synchronized void setWlanAvailable(int _wlanAvailable) {
        wlanAvail = _wlanAvailable;
    }
    public synchronized void setWlanActive(int _wlanActive) {
        wlanActive = _wlanActive;
    }

    public synchronized int getMobileAvailable(){
        return mobileAvail;
    }
    public synchronized int getMobileActive(){
        return mobileActive;
    }
    public synchronized int getWLANAvailable(){
        return wlanAvail;
    }
    public synchronized int getWLANActive(){
        return wlanActive;
    }

    public synchronized int getGpsData(){
        return gpsData;
    }
    public synchronized int getVibration() {
        return vibration;
    }
    public synchronized void setGpsData(int _gpsData){
        gpsData = _gpsData;
    }
    public synchronized void setVibration(int _vibration) {
        vibration = _vibration;
    }

    public synchronized void setCameraPicture(byte[] _cameraPicture) { cameraPicture = _cameraPicture;}
    public synchronized byte[] getCameraPicture() { return cameraPicture;}
}
