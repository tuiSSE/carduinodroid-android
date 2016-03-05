package tuisse.carduinodroid_android.data;


import android.graphics.drawable.LayerDrawable;
import tuisse.carduinodroid_android.R;

/**
 * <h1>CarduinoDroidData Database</h1>
 * The CarDuinoDroidData Class implements a full database for all the values transmitted between
 * transceiver and remote side. It is divived in camera and sound settings, mobile and WLAN
 * information, battery status, vibration for a possible gamepad.
 *
 * @author Till Max Schwikal
 * @author Lars Vogel
 * @version 1.0
 * @since 03.02.2016
 */
public class CarduinoDroidData implements CarduinoDroidIF{

    private final String TAG = "CarduinoDroidData";

    private ConnectionState ipState;
    private IpType ipType;

    private String[] cameraSupportedSizes; // All Resolutions/Sizes supported by the Transceiver
    private int cameraType; // Front=0 - Back=1
    private int cameraResolutionID; // Defines resolution ID in the array (from 0 high to n low)
    private int cameraFlashlight; // On=1 - Off=0;
    private int cameraQuality; // Value between 0(low) to 100 (high)

    private int soundPlay; // Play a horn sound=1 - No horn sound=0;
    private int soundRecord; // Start Recording=1 - Stop Recording=0;

    private float vibration; // a certain vibration value given by the motion hardware
    private float batteryPhone; // actual battery status of the transceiver

    private String gpsData; // GPS data written in CSV to get long., lat. and altitude
    private int mobileAvail; // Mobile Available: Yes 1 - No 0
    private int mobileActive; // Mobile Active: Yes 1 - No 0
    private int wlanAvail; // WLAN Available: Yes 1 - No 0
    private int wlanActive; // WLAN Active: Yes 1 - No 0

    private String remoteIp;
    private String transceiverIp;
    private String myIp;

    private byte[] cameraPicture;

    /**
     * Constructor to init all important variables at the beginning to create a default state or
     * reset at a certain points.
     */
    public CarduinoDroidData(){
        super();

        setIpState(new ConnectionState(ConnectionEnum.IDLE));
        setIpType(IpType.WLAN);

        setTransceiverIp("");
        setRemoteIp("");
        setMyIp("");

        setCameraType(1);
        setCameraResolutionID(-1);
        setCameraFlashlight(0);
        setCameraQuality(50);

        setSoundPlay(0);
        setSoundRecord(0);

        setVibration(0);
        setBatteryPhone(100);

        setGpsData("");
        setMobileActive(0);
        setMobileAvailable(0);

        setWlanActive(0);
        setWlanAvailable(0);
    }

    /**
     * This method resets all camera values if a new connection is established to protect high
     * traffic
     */
    public synchronized void resetValues(){

        setCameraSupportedSizes(null);
        setCameraType(1);
        setCameraResolutionID(-1);
        setCameraFlashlight(0);
        setCameraQuality(50);

        setSoundPlay(0);
        setSoundRecord(0);

        setVibration(0);
    }

    /**
     * Depending on the IP Connection State this methods give a defined LayerDrawable back
     * @return LayerDrawable to show the IP State on the Activity
     */
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

    /**
     * This method is the Getter for the Remote IP
     * @return IP of the connected Remote Device
     */
    public synchronized String getRemoteIp(){
        return remoteIp;
    }

    /**
     * This method is the Setter for the Remote IP
     * @param _remoteIp defined by the IP Connection Class after an established Connection to the
     *                  Data Socket
     */
    public synchronized void setRemoteIp(String _remoteIp){
        remoteIp = _remoteIp;
    }

    /**
     * This method is the Getter for the Transceiver IP
     * @return IP of the connected Transceiver Device
     */
    public synchronized String getTransceiverIp(){
        return transceiverIp;
    }

    /**
     * This method is the Getter for the Remote IP
     * @param _transceiverIp defined by the Status Activity to a choosen transceiver IP
     */
    public synchronized void setTransceiverIp(String _transceiverIp){
        transceiverIp = _transceiverIp;
    }

    /**
     * This method is the Getter for the Own IP in a choosen network
     * @return IP of the used device in its choosen network
     */
    public synchronized String getMyIp(){
        return myIp;
    }

    /**
     * This method is the Setter for the Own IP in a choosen network
     * @param _myIp IP definied by the choosen network working in
     */
    public synchronized void setMyIp(String _myIp){
        myIp = _myIp;
    }

    /**
     * This method is the Getter for the IP Connection State
     * @return the current IP State for certain operations
     */
    public synchronized ConnectionState getIpState(){
        return ipState;
    }

    /**
     * This method is the Setter for the IP Connection State
     * @param is sets the IP Connections State if you reach a certain point or an error occurs
     */
    public synchronized void setIpState(ConnectionState is){
        ipState = is;
    }

    /**
     * This method is the Getter for the IP Connection Type
     * @return the current IP Type for certain operations
     */
    public synchronized IpType getIpType(){
        return ipType;
    }

    /**
     * This method is the Setter for the IP Connection Type
     * @param it sets the IP Connections Type
     */
    public synchronized void setIpType(IpType it){
        ipType = it;
    }

    /**
     * This method is the Getter for all possible camera resolution sizes
     * @return all possible resolution sizes of the transceiver
     */
    public synchronized String[] getCameraSupportedSizes() {
        return cameraSupportedSizes;
    }

    /**
     * This method is the Getter for the chosen camera type (front/back)
     * @return if the user has chosen the front (0) or back (1) camera
     */
    public synchronized int getCameraType(){
        return cameraType;
    }

    /**
     * This method is the Getter for the chosen camera resolution
     * @return the ID of the chosen camera resolution beginning by 0 (high) to n (low)
     */
    public synchronized int getCameraResolutionID(){
        return cameraResolutionID;
    }

    /**
     * This method is the Getter for the flashlight
     * @return if the user has chosen the flashlight on (1) or off (0)
     */
    public synchronized int getCameraFlashlight(){
        return cameraFlashlight;
    }

    /**
     * This method is the Getter for the chosen camera picture quality used while compression
     * @return a value between 0 (low) and 100 (high) for the quality while compression
     */
    public synchronized int getCameraQuality(){
        return cameraQuality;
    }

    /**
     * This method is the Getter if a Sound should be played
     * @return if the user wants to hear a sound (1) or if it is stopped (0)
     */
    public synchronized int getSoundPlay(){
        return soundPlay;
    }

    /**
     * This method is the Getter if the Sound of a the area shall be recorded
     * @return if the user has chosen to record (1) or not record (0) the area sound
     */
    public synchronized int getSoundRecord(){
        return soundRecord;
    }

    /**
     * This method is the Setter for all possible camera resolution sizes
     * @param _cameraSupportedSizes is an Array with each elements is a certain resolution size
     */
    public synchronized void setCameraSupportedSizes(String[] _cameraSupportedSizes) {
        cameraSupportedSizes = _cameraSupportedSizes;
    }

    /**
     * This method is the Setter for the chosen camera type (front/back)
     * @param _cameraType for front (0) or back (1) camera
     */
    public synchronized void setCameraType(int _cameraType) {
        cameraType = _cameraType;
    }

    /**
     * This method is the Setter for the chosen camera resolution
     * @param _cameraResolutionID for the chosen camera resolution beginning by 0 (high) to n (low)
     */
    public synchronized void setCameraResolutionID(int _cameraResolutionID) {
        cameraResolutionID = _cameraResolutionID;
    }

    /**
     * This method is the Setter for the flashlight
     * @param _cameraFlashlight if the flashlight is on (1) or off (0)
     */
    public synchronized void setCameraFlashlight(int _cameraFlashlight) {
        cameraFlashlight = _cameraFlashlight;
    }

    /**
     * This method is the Setter for the chosen camera picture quality used while compression
     * @param _cameraQuality between 0 (low) and 100 (high) for the quality while compression
     */
    public synchronized void setCameraQuality(int _cameraQuality) {
        cameraQuality = _cameraQuality;
    }

    /**
     * This method is the Setter if a Sound should be played
     * @param _soundPlay sets if the user wants to hear a sound (1) or if it is stopped (0)
     */
    public synchronized void setSoundPlay(int _soundPlay) {
        soundPlay = _soundPlay;
    }

    /**
     * This method is the Setter if the Sound of a the area shall be recorded
     * @param _soundRecord sets if the user has chosen to record (1) or not record (0) the area sound
     */
    public synchronized void setSoundRecord(int _soundRecord) {
        soundRecord = _soundRecord;
    }

    /**
     * This method is the Setter if mobile connection is available at the transceiver
     * @param _mobileAvailable sets if a transceiver can use a mobile connection
     */
    public synchronized void setMobileAvailable(int _mobileAvailable) {
        mobileAvail = _mobileAvailable;
    }

    /**
     * This method is the Setter if mobile connection is active at the transceiver
     * @param _mobileActive sets if a transceiver has an active mobile connection
     */
    public synchronized void setMobileActive(int _mobileActive) {
        mobileActive = _mobileActive;
    }

    /**
     * This method is the Setter if wlan is available at the transceiver
     * @param _wlanAvailable sets if a transceiver can use wlan
     */
    public synchronized void setWlanAvailable(int _wlanAvailable) {
        wlanAvail = _wlanAvailable;
    }

    /**
     * This method is the Setter if wlan is active at the transceiver
     * @param _wlanActive sets if a transceiver has an active wlan connection
     */
    public synchronized void setWlanActive(int _wlanActive) {
        wlanActive = _wlanActive;
    }

    /**
     * This method is the Getter if mobile connection is available at the transceiver
     * @return if a transceiver can use a mobile connection (1) or not (0)
     */
    public synchronized int getMobileAvailable(){
        return mobileAvail;
    }

    /**
     * This method is the Getter if mobile connection is active at the transceiver
     * @return if a transceiver has an active mobile connection (1) or not (0)
     */
    public synchronized int getMobileActive(){
        return mobileActive;
    }

    /**
     * This method is the Getter if wlan is available at the transceiver
     * @return if a transceiver can use wlan (1) or not (0)
     */
    public synchronized int getWLANAvailable(){
        return wlanAvail;
    }

    /**
     * This method is the Getter if wlan is active at the transceiver
     * @return if a transceiver has an active wlan connection (1) or not (0)
     */
    public synchronized int getWLANActive(){
        return wlanActive;
    }

    /**
     * This method is the Getter for longitude, latitude and altitude of the actual gps data
     * @return long, lat and alt as CSV
     */
    public synchronized String getGpsData(){
        return gpsData;
    }

    /**
     * This method is the Getter for a certain motion value symbolize the Vibration for Gamepads
     * @return vibration as value over a certain amount of values to get a progress
     */
    public synchronized float getVibration() {
        return vibration;
    }

    /**
     * This method is the Getter for actual battery status of the transceiver
     * @return the battery status as a value from 1 to 100 (0 should initiate the shut down of
     * the device)
     */
    public synchronized float getBatteryPhone() {
        return batteryPhone;
    }

    /**
     * This method is the Setter for longitude, latitude and altitude of the actual gps data
     * @param _gpsData describes long, lat and alt as CSV string
     */
    public synchronized void setGpsData(String _gpsData){
        gpsData = _gpsData;
    }

    /**
     * This method is the Setter for a certain motion value symbolize the Vibration for Gamepads
     * @param _vibration is as progress value over a certain amount of values
     */
    public synchronized void setVibration(float _vibration) {
        vibration = _vibration;
    }

    /**
     * This method is the Setter for actual battery status of the transceiver
     * @param _battery shows the status as a value from 1 to 100 (0 should initiate the shut down of
     * the device)
     */
    public synchronized void setBatteryPhone (float _battery) {
        batteryPhone = _battery;
    }

    /**
     * This method is the Setter for the newest camera picture as Byte-Array
     * @param _cameraPicture contains the newest camera picture as a Byte-Array but already
     *                       compressed (camera Quality) to a YUV image
     */
    public synchronized void setCameraPicture(byte[] _cameraPicture) {
        cameraPicture = _cameraPicture;
    }

    /**
     * This method is the Getter for the newest camera picture as Byte-Array
     * @return thenewest camera picture as a Byte-Array
     */
    public synchronized byte[] getCameraPicture() {
        return cameraPicture;
    }
}
