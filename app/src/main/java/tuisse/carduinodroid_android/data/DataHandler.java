package tuisse.carduinodroid_android.data;


import android.util.Log;

import org.json.JSONObject;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;

/**
 * <h1>Data Handler</h1>
 * The data handler is a guard to all data. It holds the CarduinoData and CarduinoDroidData.
 * Also here are general app settings stored.
 *
 * @author Till Max Schwikal
 * @since 10.12.2015
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.CarduinodroidApplication
 */
public class DataHandler implements SerialFrameIF, IpFrameIF{
    private final String TAG = "CarduinoDataHandler";

    /**
     * the following variables are instantiated in the CarduinodroidApplication
     */
    protected CarduinoData cd;///<instance of the Carduino Database
    protected CarduinoDroidData ccd;///< instance of the Carduinodroid Database
    protected SerialFrameHandler serialFrameHandler;///< instance of the serial frame handler
    protected IpFrameHandler ipFrameHandler;///< instance of the ip frame handler

    private ControlMode controlMode = null;///< global control mode instance
    private SerialType serialPref = SerialType.NONE;///< preference of the serial connection type
    private String bluetoothDeviceName = "";///< preference filter of bluetooth device name
    private BluetoothHandling bluetoothHandling = BluetoothHandling.AUTO;///< bluetooth handling instance
    private boolean failSafeStopPref = true;///< preference of the failsafe stop variable
    private boolean debugView = false;///< preference of the debug view variable
    private boolean bluetoothEnabled = false;///< bluetooth enabled variable
    private CommunicationStatus communicationStatus = CommunicationStatus.IDLE;///< communication status variable
    private int screensaver = 60000;///< preference of the screensaver timeout


    /**
     * public constructor
     *
     * empty because the data handler variables are set on startup in CarduinodroidApplication
     */
    public DataHandler() {
    }

    /**
     * global getter of the carduino database
     * @return cd carduino database
     */
    public CarduinoData getData(){
        return cd;
    }


    /**
     * global getter of the carduinodroid database
     * @return cd carduinodroid database
     */
    public CarduinoDroidData getDData(){
        return ccd;
    }


    /**
     * setter for the screensaver value
     * @param ss screensavervalue in ms 0 for inactivity of the screensaver
     */
    public synchronized void setScreensaver(int ss){
        screensaver = ss;
    }

    /**
     * getter of the screensaver value
     * @return screensaver timeout in ms
     */
    public synchronized int getScreensaver(){
        return screensaver;
    }

    /**
     * setter for the bluetooth enabled variable
     * @param bte if set, the bluetooth module was enabled before starting a bluetooth connection
     */
    public synchronized void setBluetoothEnabled(boolean bte){
        bluetoothEnabled = bte;
    }

    /**
     * getter for the bluetooth enabled variable
     * @return bluetoothEnabled if set, the bluetooth module was enabled before starting a bluetooth connection
     */
    public synchronized boolean getBluetoothEnabled(){
        return bluetoothEnabled;
    }

    /**
     * getter of the communication status
     * @return communicationStatus
     * @see CommunicationStatus
     */
    public synchronized CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    /**
     * calculates the communication status based on the control mode, ipStatus and serialStatus
     * @see CommunicationStatus
     * @see ControlMode
     * @see CarduinoData
     * @see CarduinoDroidData
     * @see ConnectionState
     */
    public synchronized void calcCommunicationStatus(){
        switch (controlMode){
            case REMOTE:
                if(getDData().getIpState().isRunning()){
                    if(getData().getSerialState().isRunning()){
                        communicationStatus = CommunicationStatus.OK;
                        return;
                    }
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                    return;
                }
                if(getDData().getIpState().isError()){
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.BOTH_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_ERROR;
                    return;
                }
                if(getDData().getIpState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else{
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_CONNECTING;
                    return;
                //}
            case TRANSCEIVER:
                if(getDData().getIpState().isRunning()){
                    if(getData().getSerialState().isRunning()){
                        communicationStatus = CommunicationStatus.OK;
                        return;
                    }
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.SERIAL_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                    return;
                }
                if(getDData().getIpState().isError()){
                    if(getData().getSerialState().isError()){
                        communicationStatus = CommunicationStatus.BOTH_ERROR;
                        return;
                    }
                    communicationStatus = CommunicationStatus.IP_ERROR;
                    return;
                }
                if(getDData().getIpState().isIdle() && getData().getSerialState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else{
                if(getData().getSerialState().isRunning()){
                    communicationStatus = CommunicationStatus.IP_CONNECTING;
                    return;
                }
                if(getData().getSerialState().isError()){
                    communicationStatus = CommunicationStatus.SERIAL_ERROR;
                    return;
                }
                communicationStatus = CommunicationStatus.BOTH_CONNECTING;
                return;
            //}
            default://DIRECT
                if(getData().getSerialState().isRunning()){
                    communicationStatus = CommunicationStatus.OK;
                    return;
                }
                if(getData().getSerialState().isError()){
                    communicationStatus = CommunicationStatus.SERIAL_ERROR;
                    return;
                }
                if( getData().getSerialState().isIdle()){
                    communicationStatus = CommunicationStatus.IDLE;
                    return;
                }
                //else
                communicationStatus = CommunicationStatus.SERIAL_CONNECTING;
                return;
        }
    }

    /**
     * based on the communication status the app displays the status in different colors
     * @return resource id of the color of the actual communication status
     */
    public synchronized int getCommunicationStatusColor(){
        int color = R.color.colorCommunicationStatusOk;
        if(getCommunicationStatus().isIdleError()){
            color = R.color.colorCommunicationStatusIdleError;
        }
        if(getCommunicationStatus().isConnecting()){
            color = R.color.colorCommunicationStatusConnecting;
        }
        return color;
    }

    /**
     * based on the communication status a string is displayed
     * @return displayable communication status string
     */
    public synchronized String getCommunicationStatusString(){
        String s = "";
        switch (communicationStatus){
            case IDLE:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusIdle);
                break;
            case BOTH_CONNECTING:
            case IP_CONNECTING:
            case SERIAL_CONNECTING:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusConnecting);
                break;
            case OK:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusOk);
                break;
            case ERROR:
            case BOTH_ERROR:
            case IP_ERROR:
            case SERIAL_ERROR:
                s = CarduinodroidApplication.getAppContext().getString(R.string.communicationStatusError);
                break;
            default:
                s = "";
                Log.e(TAG, "getCommunicationStatusString: unknown communication status");
                break;
        }
        return s;
    }

    /**
     * setter of the control mode, if the control mode is changed, the global behavior of the whole
     * needs to be changed
     * the function instanziated new CarduinoData and/or CarduinoDroidData objects
     * @param cm new control mode which should be set
     */
    public synchronized void setControlMode(ControlMode cm){
        try {
            if (controlMode == null || cd == null) {
                cd = new CarduinoData();
                ccd = new CarduinoDroidData();
            }
            else {
                switch (controlMode) {
                    case TRANSCEIVER: {
                        switch (cm) {
                            case TRANSCEIVER:
                                //do nothing
                                break;
                            case REMOTE:
                                cd = new CarduinoData();

                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = new CarduinoDroidData();
                                break;
                            default://DIRECT
                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //cd stays the same
                                //ccd = null;
                                break;
                        }
                        break;
                    }
                    case REMOTE: {
                        switch (cm) {
                            case TRANSCEIVER:
                                cd = new CarduinoData();
                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = new CarduinoDroidData();
                                break;
                            case REMOTE:
                                //do nothing
                                break;
                            default://DIRECT
                                cd = new CarduinoData();
                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                //ccd = null;
                                break;
                        }
                        break;
                    }
                    default: {//Direct
                        switch (cm) {
                            case TRANSCEIVER:
                                //cd stays the same
                                //ccd = new CarduinoDroidData();
                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                break;
                            case REMOTE:
                                cd = new CarduinoData();
                                //ccd = new CarduinoDroidData();
                                getDData().resetValues();
                                ccd.setIpState(new ConnectionState(ConnectionEnum.IDLE));
                                break;
                            default://DIRECT
                                //do nothing
                                break;
                        }
                        break;
                    }
                }
            }
            controlMode = cm;
            if(cd == null){
                Log.e(TAG, "no CarduinoData");
            }
            serialFrameHandler = new SerialFrameHandler(cd);
            if(!controlMode.isDirect()){
                if(ccd == null){
                    Log.e(TAG, "no CarduinoDroidData");
                }
                ipFrameHandler = new IpFrameHandler(cd,ccd);
            }
            else{
                ipFrameHandler = null;
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    /**
     * getter of the actual control mode
     * @return control mode
     */
    public synchronized ControlMode getControlMode(){
        return controlMode;
    }

    /**
     * switch function to set the "next" control mode
     * @return integer of the control mode for preference
     */
    public synchronized int setControlModeNext(){
        switch (controlMode){
            case TRANSCEIVER:
                setControlMode(ControlMode.REMOTE);
                break;
            case REMOTE:
                setControlMode(ControlMode.DIRECT);
                break;
            case DIRECT:
                setControlMode(ControlMode.TRANSCEIVER);
                break;
            default:
                setControlMode(ControlMode.TRANSCEIVER);
                Log.e(TAG, "error in setControlModeNext, set ControlMode.Transceiver");
                break;
        }
        return ControlMode.toInteger(controlMode);
    }

    /**
     * switch function to set the "previous" control mode
     * @return integer of the control mode for preference
     */
    public synchronized int setControlModePrev(){
        int cm;
        try {
            switch (controlMode){
                case TRANSCEIVER:
                    setControlMode(ControlMode.DIRECT);
                    break;
                case REMOTE:
                    setControlMode(ControlMode.TRANSCEIVER);
                    break;
                case DIRECT:
                    setControlMode(ControlMode.REMOTE);
                    break;
                default:
                    Log.e(TAG, "error in setControlModePrev, set ControlMode.Transceiver");
                    setControlMode(ControlMode.TRANSCEIVER);
                    break;
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }finally {
             cm = ControlMode.toInteger(controlMode);
        }
        return cm;
    }

    /**
     * setter of the failsafe stop preference
     * @param fss true if failsafe stop is set, false otherwise
     */
    public synchronized void setFailSafeStopPref(boolean fss){
        failSafeStopPref = fss;
    }

    /**
     * getter of the failsafe stop preference
     * @return true if failsafe stop is set, false otherwise
     */
    public synchronized final boolean getFailSafeStopPref(){
        return failSafeStopPref;
    }

    /**
     * toggles the failsafe stop value
     * @return integer of the failsafe stop value for preference
     */
    public synchronized int toggleFailSafeStopPref(){
        failSafeStopPref = !failSafeStopPref;
        if(failSafeStopPref){
            return 1;
        }
        return 0;
    }

    /**
     * setter of the debug view variable
     * @param dv true if debug view should be visible, false otherwise
     */
    public synchronized void setDebugView(boolean dv){
        debugView = dv;
    }

    /**
     * getter of the debug view variable
     * @return true if debug view should be visible, false otherwise
     */
    public synchronized final boolean getDebugView(){
        return debugView;
    }

    /**
     * toggles the debug view variable
     * @return integer of the debug view value for preferences
     */
    public synchronized int toggleDebugView(){
        debugView = !debugView;
        if(debugView){
            return 1;
        }
        return 0;
    }

    /**
     * getter of the bluetooth handling
     * @return bluetooth handling which can be AUTO, ON or OFF
     * @see BluetoothHandling
     */
    public synchronized final BluetoothHandling getBluetoothHandling(){
        return bluetoothHandling;
    }

    /**
     * /**
     * setter of the bluetooth handling
     * @param bth bluetooth handling which can be AUTO, ON or OFF
     * @see BluetoothHandling
     */
    public synchronized void setBluetoothHandling(BluetoothHandling bth){
        bluetoothHandling = bth;
    }

    /**
     * setter of the serial type
     * @return serial type which can be USB or BLUETOOTH
     * @see SerialType
     */
    public synchronized final SerialType getSerialPref(){
        return serialPref;
    }
    /**
     * getter of the serial type
     * @param t serial type which can be USB or BLUETOOTH
     * @see SerialType
     */
    public synchronized void setSerialPref(SerialType t){
        serialPref = t;
    }

    /**
     * sets the bluetooth device name which is a filter for the different bluetooth connections
     * @param name String which needs to be inside the bluetooth's device name. It should
     *             differentiate all other peered bluetooth devices.
     */
    public synchronized void setBluetoothDeviceName(String name){
        bluetoothDeviceName = name;
    }

    /**
     * getter of the bluetooth device name
     * @return String which needs to be inside the bluetooth's device name. It should
     *             differentiate all other peered bluetooth devices.
     */
    public synchronized final String getBluetoothDeviceName(){
        return bluetoothDeviceName;
    }

    /**
     * interfaced serial frame assemble function in tx direction based on the control mode
     * @return byte[] which is an assembled serial frame; in ControlMode.REMOTE an empty byte
     * @see SerialFrameIF
     * @see ControlMode
     */
    public synchronized byte[] serialFrameAssembleTx() {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAssembleTx: " + controlMode.toString());
            return new byte[0];
        }
        else{
            return serialFrameHandler.serialFrameAssembleTx();
        }
    }

    /**
     * interfaced serial frame byte append function in rx direction based on the control mode
     * @return true if a valid serial frame was received on the last byte input
     * @param inChar next read char on the serial buffer
     * @see SerialFrameIF
     * @see ControlMode
     */
    public synchronized boolean serialFrameAppendRx(byte inChar) {
        if(controlMode.isRemote()){
            Log.e(TAG, "serialFrameAppendRx: " + controlMode.toString());
            return false;
        }
        else{
            return serialFrameHandler.serialFrameAppendRx(inChar);
        }
    }

    /**
     * interfaced parse JSON function based on the control mode
     * parses a received JSON string
     * @return types of detected string modules in a string
     * @param jsonObjectRxData incoming string which should be parsed
     * @see IpFrameIF
     * @see ControlMode
     */
    public synchronized String parseJson(String jsonObjectRxData) {
        if(controlMode.isDirect()){
            return "false";
        }
        return ipFrameHandler.parseJson(jsonObjectRxData);
    }

    /**
     * interfaced get data to transmit function based on control mode
     * @param dataTypeMask mask of the JSÒN data type, information which parts the needed frame
     *                     should consist of
     * @param dataServerStatus is important if this method is only used for control messages
     * @return the created JSON or null object
     */
    @Override
    public synchronized JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus) {
        if(controlMode.isDirect()){
            return null;
        }
        return ipFrameHandler.getTransmitData(dataTypeMask,dataServerStatus);
    }
}