package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialDataTx extends SerialData {
    private final String TAG = "CarduinoSerialDataTx";

    private final int numSpeed = 2;
    private final int numSteer = 3;
    private final int numStatus = 4;
    private final int numCheck = 5;

    //byte 0 Start
    //byte 1 Version+Length
    private final int length = 3;
    private final int bufferLength = length + bufferLengthOffset;
    //byte 2 Speed
    private final int speedMax = 127;//forwards
    private final int speedMin = -127;//backwards
    //byte 3 Steer
    private final int steerMax = 127;//right
    private final int steerMin = -127;//left
    //byte 4
    private final int statusLedShift = 0;
    private final int statusLedMask = 1 << statusLedShift;
    private final int frontLightShift = 1;
    private final int frontLightMask = 1 << frontLightShift;
    private final int resetAccCurShift = 4;
    private final int resetAccCurMask = 1 << resetAccCurShift;
    private final int failSafeStopShift = 5;
    private final int failSafeStopMask = 1 << failSafeStopShift;

    private int speed;
    private int steer;
    private int statusLed;
    private int frontLight;
    private int failSafeStop;
    private int resetAccCur;

    public SerialDataTx(){
        reset();
    }

    public synchronized void reset(){
        speed = 0;
        steer = 0;
        statusLed = 0;
        frontLight = 0;
        resetAccCur = 0;
        failSafeStop = 1;
    }

    public synchronized String print(){
        return  " version      "+ version+
                " length       "+ length+
                " speedVal     "+ speed+
                " steerVal     "+ steer+
                " statusLed    "+ statusLed+
                " frontLight   "+ frontLight+
                " failSafeStop "+ failSafeStop+
                " resetAccCur  "+ resetAccCur;
    }

    public synchronized byte[] get() {
        byte[] command = new byte[bufferLength];
        command[0] = startByte;
        command[1] = getVersionLength(length);
        command[numSpeed] = getSpeed();
        command[numSteer] = getSteer();
        command[numStatus] = getStatus();
        command[numCheck] = getCheck(command, numCheck);
        return command;
    }

    public synchronized byte getSpeed(){
        if((byte) speed == startByte){
            Log.e(TAG, "speed must not be -128");
            speed = 0;
        }
        return (byte) speed;
    }

    public synchronized byte getSteer(){
        if((byte) steer == startByte){
            Log.e(TAG, "steer must not be -128");
            steer = 0;
        }
        return (byte) steer;
    }

    public synchronized byte getStatus(){
        return (byte) (0x00
                | ((statusLed << statusLedShift) & statusLedMask)
                | ((frontLight << frontLightShift) & frontLightMask)
                | ((failSafeStop << failSafeStopShift) & failSafeStopMask)
                | ((resetAccCur << resetAccCurShift) & resetAccCurMask));
    }

    public synchronized void setSpeed(int _speed) {
        if ((_speed < speedMin) || (_speed > speedMax)) {
            Log.e(TAG, "setSpeed out of bounds" + _speed);
        }
        else {
            speed = _speed;
        }
    }

    public synchronized void setSteer(int _steer) {
        if ((_steer < steerMin) || (_steer > steerMax)) {
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
