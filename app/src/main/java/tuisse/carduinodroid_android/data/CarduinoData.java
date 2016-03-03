package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;
import tuisse.carduinodroid_android.Utils;

/**
 * @author Till Max Schwikal
 * @date 04.01.2016
 *
 * Class which holds all data sent and received from the Arduino Device.
 *
 * This class has the role of a central data storage for the carduinodroid. All data needed for the
 * Arduino are stored here. The class provides besides some helper functions getter and setter to
 * the data.
 *
 * @see CarduinoIF
 * @see DataHandler
 */

public class CarduinoData implements CarduinoIF{
    private final String TAG = "CarduinoData";

    protected ConnectionState serialState;///< holds the serial state
    protected SerialType serialType;///< holds the serial type
    protected String serialName;///< holds the serial name (device name which is connected)

    protected int current;///< holds the value of the received current of the battery in 0.1mA
    protected int absBattCap;///< holds the value of the received absolute battery capacity in 1mAh
    protected int relBattCap;///< holds the value of the received relative battery capacity in %
    protected int voltage;///< holds the value of the received voltage in 0.1V
    protected int temperature;///< holds the value of the received temperature in 0.5°C
    protected int ultrasoundFront;///< holds the value of the received front ultrasound distance in 1cm
    protected int ultrasoundBack;///< holds the value of the received back ultrasound distance in 1cm

    protected int speed;///< holds the speed value in the carduinodroid device (between VAL_SPEED_MAX and VAL_SPEED_MIN)
    protected int steer;///< holds the steering value in the carduinodroid device (between VAL_STEER_MAX and VAL_STEER_MIN)
    protected int statusLed;///< holds the status LED value in the carduinodroid device (0: OFF 1:ON)
    protected int frontLight;///< holds the front light value in the carduinodroid device (0: OFF 1:ON)
    protected int failSafeStop;///< holds the failsafe stop value in the carduinodroid device (0: OFF 1:ON)
    protected int resetAccCur;///< holds the front light value in the carduinodroid device (0: OFF 1:ON)


    /**
     * constructor of the class. All variables are initialized.
     */
    public CarduinoData(){
        serialState = new ConnectionState(ConnectionEnum.IDLE,"");
        setSerialName(CarduinodroidApplication.getAppContext().getString(R.string.serialDeviceNone));
        setSerialType(SerialType.NONE);
        setCurrent(0);
        setAbsBattCap(0);
        setRelBattCap(0);
        setVoltage(0);
        setTemperature(0);
        setUltrasoundBack(0);
        setUltrasoundFront(0);
        setStatusLed(0);
        setFrontLight(0);
        setResetAccCur(0);
        setFailSafeStop(0);
        resetMotors();
    }

    /**
     * Helper function to get the contents of the CarduinoData class as string for printing
     * @return debug string
     */
    public synchronized String print(){
        return  " current         " + current+
                " absCapacity     " + absBattCap+
                " relCapacity     " + relBattCap+
                " voltage         " + voltage+
                " temperature     " + temperature+
                " ultrasoundFront " + ultrasoundFront+
                " ultrasoundBack  " + ultrasoundBack+
                " speedVal        " + speed+
                " steerVal        " + steer+
                " statusLed       " + statusLed+
                " frontLight      " + frontLight+
                " failSafeStop    " + failSafeStop+
                " resetAccCur     " + resetAccCur;
    }

    /**
     * Function to reset and stop the motors.
     */
    public synchronized void resetMotors() {
        speed = 0;
        steer = 0;
    }


    /**
     * setter for the serial state
     * @param state
     */
    public synchronized void setSerialState(ConnectionState state){
        serialState = state;
    }

    /**
     * getter for the serial state
     * @return state
     */
    public synchronized ConnectionState getSerialState(){
        return serialState;
    }

    /**
     * setter for the serial type
     * @param type
     */
    public synchronized void setSerialType(SerialType type){
        serialType = type;
    }

    /**
     * getter for the serial type
     * @return type
     */
    public synchronized SerialType getSerialType(){
        return serialType;
    }

    /**
     * setter for the serial name
     * @param name
     */
    public synchronized void setSerialName(String name){
        serialName = name;
    }

    /**
     * getter for the serial name
     * @return name
     */
    public synchronized String getSerialName(){
        return serialName;
    }

    /**
     * Helper functions which returns the actual logo depending on the serialState
     * @param serialPref
     * @return logo
     */
    public synchronized LayerDrawable getSerialConnLogoId(SerialType serialPref){
        int state;
        int type;
        switch (serialState.getState()){
            case  TRYFIND:
            case  FOUND:
            case  TRYCONNECT:
                state = R.drawable.status_try_connect;
                break;
            case  CONNECTED:
            case  RUNNING:
                state = R.drawable.status_connected;
                break;
            case ERROR:
                state = R.drawable.status_error;
                break;
            case STREAMERROR:
                state = R.drawable.status_connected_error;
                break;
            case TRYCONNECTERROR:
                state = R.drawable.status_try_connect_error;
                break;
            case UNKNOWN:
                state = R.drawable.status_unknown;
                break;
            default:
                state = R.drawable.status_idle;
                break;
        }
        if(serialState.isUnknown()){
            type = R.drawable.serial_type_none;
        }
        else{
            switch (serialType){
                case BLUETOOTH:
                    if(serialPref.isAuto())
                        type = R.drawable.serial_type_auto_bt;
                    else
                        type = R.drawable.serial_type_bt;
                    break;
                case USB:
                    if(serialPref.isAuto())
                        type = R.drawable.serial_type_auto_usb;
                    else
                        type = R.drawable.serial_type_usb;
                    break;
                case AUTO:
                    //should never happen
                    Log.e(TAG, "serialType is Auto");
                    type = R.drawable.serial_type_none;
                    break;
                default://NONE
                    switch (serialPref){
                        case USB:
                            type = R.drawable.serial_type_none_usb;
                            break;
                        case BLUETOOTH:
                            type = R.drawable.serial_type_none_bt;
                            break;
                        case AUTO:
                            type = R.drawable.serial_type_none_auto;
                            break;
                        default://NONE
                            //should never happen
                            Log.e(TAG, "serialType is NONE, serialPref is NONE");
                            type = R.drawable.serial_type_none;
                            break;
                    }
            }
        }
        return Utils.assembleDrawables(state, type);
    }

    /**
     * setter of the cardiunodroid current
     * @param _current
     */
    public synchronized void setCurrent(int _current) {
        current = _current;
    }

    /**
     * setter of the absolute battery capacity
     * @param _absBattCap
     */
    public synchronized void setAbsBattCap(int _absBattCap) {
        absBattCap = _absBattCap;
    }

    /**
     * setter of the relative battery capacity
     * @param _relBattCap
     */
    public synchronized void setRelBattCap(int _relBattCap) {
        relBattCap = _relBattCap;
    }

    /**
     * setter of the carduinodroid voltage
     * @param _voltage
     */
    public synchronized void setVoltage(int _voltage) {
        voltage = _voltage;
    }

    /**
     * setter of the carduinodroid temperature
     * @param _temperature
     */
    public synchronized void setTemperature(int _temperature) {
        temperature = _temperature;
    }

    /**
     * setter of the carduinodroid front ultrasound distance
     * @param _ultrasoundFront
     */
    public synchronized void setUltrasoundFront(int _ultrasoundFront) {
        if (_ultrasoundFront == BYTE_MASK) _ultrasoundFront = -1;
        ultrasoundFront = _ultrasoundFront;
    }

    /**
     * setter of the carduinodroid back ultrasound distance
     * @param _ultrasoundBack
     */
    public synchronized void setUltrasoundBack(int _ultrasoundBack) {
        if (_ultrasoundBack == BYTE_MASK) _ultrasoundBack = -1;
        ultrasoundBack = _ultrasoundBack;
    }

    /**
     * getter of the carduinodroid current as integer
     * @return current
     */
    public synchronized int getCurrent() {
        return current;
    }

    /**
     * getter of the carduinodroid absolute battery capacity as integer
     * @return absolute battery capacity
     */
    public synchronized int getAbsBattCap() {
        return absBattCap;
    }

    /**
     * getter of the carduinodroid relative battery capacity as integer
     * @return relative battery capacity
     */
    public synchronized int getRelBattCap() {
        return relBattCap;
    }

    /**
     * getter of the carduinodroid voltage as integer
     * @return voltage
     */
    public synchronized int getVoltage() {
        return voltage;
    }

    /**
     * getter of the carduinodroid temperature as integer
     * @return temperature
     */
    public synchronized int getTemperature() {
        return temperature;
    }

    /**
     * getter of the carduinodroid front ultrasound distance as integer
     * @return front ultrasound distance
     */
    public synchronized int getUltrasoundFront() {
        return ultrasoundFront;
    }

    /**
     * getter of the carduinodroid back ultrasound distance as integer
     * @return back ultrasound distance
     */
    public synchronized int getUltrasoundBack() {
        return ultrasoundBack;
    }

    /**
     * getter of the carduinodroid current as float in 1A
     * @return current
     */
    public synchronized float getCurrentFloat() {
        return current / CURRENT_DEVIDER;
    }

    /**
     * getter of the carduinodroid absolute battery capacity as float in 1mAh
     * @return absolute battery capacity
     */
    public synchronized float getAbsBattCapFloat() {
        return absBattCap / ABS_BATT_DEVIDER;
    }

    /**
     * getter of the carduinodroid relative battery capacity as float in %
     * @return relative battery capacity
     */
    public synchronized float getRelBattCapFloat() {
        return relBattCap / PER_BATT_DEVIDER;
    }

    /**
     * getter of the carduinodroid voltage as float in 1V
     * @return voltage
     */
    public synchronized float getVoltageFloat() {
        return voltage / VOLTAGE_DEVIDER;
    }

    /**
     * getter of the carduinodroid temperature as float in 1*C
     * @return temperature
     */
    public synchronized float getTemperatureFloat() {
        return temperature / TEMPERATURE_DEVIDER;
    }

    /**
     * getter of the front ultrasound distance as float in 1cm
     * @return front ultrasound distance
     */
    public synchronized float getUltrasoundFrontFloat() {
        return ultrasoundFront / ULTRASOUND_FRONT_DEVIDER;
    }

    /**
     * getter of the back ultrasound distance as float in 1cm
     * @return back ultrasound distance
     */
    public synchronized float getUltrasoundBackFloat() {
        return ultrasoundBack / ULTRASOUND_BACK_DEVIDER;
    }

    /**
     * getter of the speed value
     * @return speed
     */
    public synchronized byte getSpeed(){
        if((byte) speed == START_BYTE){
            Log.e(TAG, "speed must not be -128");
            speed = 0;
        }
        return (byte) speed;
    }

    /**
     * getter of the steering value
     * @return steer
     */
    public synchronized byte getSteer(){
        if((byte) steer == START_BYTE){
            Log.e(TAG, "steer must not be -128");
            steer = 0;
        }
        return (byte) steer;
    }

    /**
     * getter of the status LED value
     * @return status led
     */
    public synchronized int getStatusLed() {
        return statusLed;
    }

    /**
     * getter of the front light value
     * @return front light
     */
    public synchronized int getFrontLight() {
        return frontLight;
    }

    /**
     * getter of the reset accumulated currency value
     * @return reset accumulated currency
     */
    public synchronized int getResetAccCur() {
        return resetAccCur;
    }

    /**
     * getter of the failsafe stop (soft break) value
     * @return failsafe stop
     */
    public synchronized int getFailSafeStop() {
        return failSafeStop;
    }

    /**
     * setter of the speed value
     * @param _speed
     */
    public synchronized void setSpeed(int _speed) {
        if ((_speed < VAL_SPEED_MIN) || (_speed > VAL_SPEED_MAX)) {
            Log.e(TAG, "setSpeed out of bounds" + _speed);
        }
        else {
            speed = _speed;
        }
    }

    /**
     * setter of the steering value
     * @param _steer
     */
    public synchronized void setSteer(int _steer) {
        if ((_steer < VAL_STEER_MIN) || (_steer > VAL_STEER_MAX)) {
            Log.e(TAG, "setSteer out of bounds" + _steer);
        }
        else {
            steer = _steer;
        }
    }

    /**
     * setter of the status LED value
     * @param _statusLed
     */
    public synchronized void setStatusLed(int _statusLed) {
        if (_statusLed != 0)
            this.statusLed = 1;//on
        else
            this.statusLed = 0;//off = default
    }

    /**
     * setter of the front light value
     * @param _frontLight
     */
    public synchronized void setFrontLight(int _frontLight) {
        if (_frontLight != 0)
            this.frontLight = 1;//on
        else
            this.frontLight = 0;//off = default
    }

    /**
     * setter of the reset accumulated current value
     * @param _resetAccCur
     */
    public synchronized void setResetAccCur(int _resetAccCur) {
        if (_resetAccCur != 0)
            this.resetAccCur = 1;//on
        else
            this.resetAccCur = 0;//off = default
    }

    /**
     * setter of the failsafe stop value
     * @param _failSafeStop
     */
    public synchronized void setFailSafeStop(int _failSafeStop) {
        if (_failSafeStop != 0)
            this.failSafeStop = 1;//on = default
        else
            this.failSafeStop = 0;//off
    }
}