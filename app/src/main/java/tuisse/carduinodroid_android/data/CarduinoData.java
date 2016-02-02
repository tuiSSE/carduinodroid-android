package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import tuisse.carduinodroid_android.CarduinodroidApplication;
import tuisse.carduinodroid_android.R;
import tuisse.carduinodroid_android.Utils;

/**
 * Created by keX on 04.01.2016.
 */
public class CarduinoData implements CarduinoIF{
    private final String TAG = "CarduinoData";

    protected final int VAL_SPEED_MAX =  SPEED_MAX;//forwards
    protected final int VAL_SPEED_MIN = -SPEED_MAX;//backwards
    protected final int VAL_STEER_MAX =  STEER_MAX;//right
    protected final int VAL_STEER_MIN = -STEER_MAX;//left
    protected final int BYTE_MASK     = 0xff;

    protected ConnectionState serialState;
    protected SerialType serialType;
    protected String serialName;

    protected int current;//in 0.1mA
    protected int absBattCap;//in 1mAh
    protected int relBattCap;//in %
    protected int voltage;//in 0.1V
    protected int temperature;//in 0.5°C
    protected int ultrasoundFront;//in 1cm
    protected int ultrasoundBack;//in 1cm

    protected int speed;
    protected int steer;
    protected int statusLed;
    protected int frontLight;
    protected int failSafeStop;
    protected int resetAccCur;

    public CarduinoData(){
        serialState = new ConnectionState(ConnectionEnum.IDLE,"");
        setSerialName(CarduinodroidApplication.getAppContext().getString(R.string.serialDeviceNone));
        setSerialType(SerialType.NONE);
        current = 0;
        absBattCap = 0;
        relBattCap = 0;
        voltage = 0;
        temperature = 0;
        ultrasoundFront = 0;
        ultrasoundBack = 0;
        resetMotors();
        statusLed = 0;
        frontLight = 0;
        resetAccCur = 0;
        failSafeStop = 1;
    }

    public CarduinoData(CarduinoData cd){
        try {
            setSerialType(cd.serialType);
            setSerialState(cd.serialState);
            setSerialName(cd.serialName);
            setCurrent(cd.current);
            setAbsBattCap(cd.absBattCap);
            setRelBattCap(cd.relBattCap);
            setVoltage(cd.voltage);
            setTemperature(cd.temperature);
            setUltrasoundFront(cd.ultrasoundFront);
            setUltrasoundBack(cd.ultrasoundBack);

            setSpeed(cd.speed);
            setSteer(cd.steer);
            setStatusLed(cd.statusLed);
            setFrontLight(cd.frontLight);
            setFailSafeStop(cd.failSafeStop);
            setResetAccCur(cd.resetAccCur);
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

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

    public synchronized void resetMotors() {
        speed = 0;
        steer = 0;
    }

    public synchronized void setSerialState(ConnectionState state){
        serialState = state;
    }

    public synchronized ConnectionState getSerialState(){
        return serialState;
    }

    public synchronized void setSerialType(SerialType type){
        serialType = type;
    }

    public synchronized SerialType getSerialType(){
        return serialType;
    }

    public synchronized void setSerialName(String s){
        serialName = s;
    }

    public synchronized String getSerialName(){
        return serialName;
    }

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

    public synchronized void setCurrent(int _current) {
        current = _current;
    }

    public synchronized void setAbsBattCap(int _absBattCap) {
        absBattCap = _absBattCap;
    }

    public synchronized void setRelBattCap(int _relBattCap) {
        relBattCap = _relBattCap;
    }

    public synchronized void setVoltage(int _voltage) {
        voltage = _voltage;
    }

    public synchronized void setTemperature(int _temperature) {
        temperature = _temperature;
    }

    public synchronized void setUltrasoundFront(int _ultrasoundFront) {
        if (_ultrasoundFront == BYTE_MASK) _ultrasoundFront = -1;
        ultrasoundFront = _ultrasoundFront;
    }

    public synchronized void setUltrasoundBack(int _ultrasoundBack) {
        if (_ultrasoundBack == BYTE_MASK) _ultrasoundBack = -1;
        ultrasoundBack = _ultrasoundBack;
    }

    public synchronized int getCurrent() {
        return current;
    }

    public synchronized int getAbsBattCap() {
        return absBattCap;
    }

    public synchronized int getRelBattCap() {
        return relBattCap;
    }

    public synchronized int getVoltage() {
        return voltage;
    }

    public synchronized int getTemperature() {
        return temperature;
    }

    public synchronized int getUltrasoundFront() {
        return ultrasoundFront;
    }

    public synchronized int getUltrasoundBack() {
        return ultrasoundBack;
    }

    public synchronized float getCurrentFloat() {
        return current / CURRENT_DEVIDER;
    }
    public synchronized float getAbsBattCapFloat() {
        return absBattCap / ABS_BATT_DEVIDER;
    }
    public synchronized float getRelBattCapFloat() {
        return relBattCap / PER_BATT_DEVIDER;
    }
    public synchronized float getVoltageFloat() {
        return voltage / VOLTAGE_DEVIDER;
    }
    public synchronized float getTemperatureFloat() {
        return temperature / TEMPERATURE_DEVIDER;
    }
    public synchronized float getUltrasoundFrontFloat() {
        return ultrasoundFront / ULTRASOUND_FRONT_DEVIDER;
    }
    public synchronized float getUltrasoundBackFloat() {
        return ultrasoundBack / ULTRASOUND_BACK_DEVIDER;
    }

    public synchronized byte getSpeed(){
        if((byte) speed == START_BYTE){
            Log.e(TAG, "speed must not be -128");
            speed = 0;
        }
        return (byte) speed;
    }

    public synchronized byte getSteer(){
        if((byte) steer == START_BYTE){
            Log.e(TAG, "steer must not be -128");
            steer = 0;
        }
        return (byte) steer;
    }

    public synchronized int getStatusLed() {
        return statusLed;
    }

    public synchronized int getFrontLight() {
        return frontLight;
    }

    public synchronized int getResetAccCur() {
        return resetAccCur;
    }

    public synchronized int getFailSafeStop() {
        return failSafeStop;
    }

    public synchronized void setSpeed(int _speed) {
        if ((_speed < VAL_SPEED_MIN) || (_speed > VAL_SPEED_MAX)) {
            Log.e(TAG, "setSpeed out of bounds" + _speed);
        }
        else {
            speed = _speed;
        }
    }

    public synchronized void setSteer(int _steer) {
        if ((_steer < VAL_STEER_MIN) || (_steer > VAL_STEER_MAX)) {
            Log.e(TAG, "setSteer out of bounds" + _steer);
        }
        else {
            steer = _steer;
        }
    }

    public synchronized void setStatusLed(int _statusLed) {
        if (_statusLed != 0)
            this.statusLed = 1;//on
        else
            this.statusLed = 0;//off = default
    }

    public synchronized void setFrontLight(int _frontLight) {
        if (_frontLight != 0)
            this.frontLight = 1;//on
        else
            this.frontLight = 0;//off = default
    }

    public synchronized void setResetAccCur(int _resetAccCur) {
        if (_resetAccCur != 0)
            this.resetAccCur = 1;//on
        else
            this.resetAccCur = 0;//off = default
    }

    public synchronized void setFailSafeStop(int _failSafeStop) {
        if (_failSafeStop != 0)
            this.failSafeStop = 1;//on = default
        else
            this.failSafeStop = 0;//off
    }
}