package tuisse.carduinodroid_android;

import android.app.Application;
import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialProtocolTx extends SerialProtocol {
    private final String TAG = "CarduinoSerialTx";

    private final int NUM_SPEED = 2;
    private final int NUM_STEER = 3;
    private final int NUM_STATUS = 4;
    private final int NUM_CHECK = 5;

    //byte 0 Start
    //byte 1 Version+Length
    private final int LENGTH = 3;
    private final int BUFFER_LENGTH = LENGTH + BUFFER_LENGTH_PROTOCOL_OFFSET;
    //byte 2 Speed
    public  final int VAL_SPEED_MAX = 127;//forwards
    public  final int VAL_SPEED_MIN = -127;//backwards
    //byte 3 Steer
    public  final int VAL_STEER_MAX = 127;//right
    public  final int VAL_STEER_MIN = -127;//left
    //byte 4
    private final int STATUS_LED_SHF = 0;
    private final int STATUS_LED_MSK = 1 << STATUS_LED_SHF;
    private final int FRONT_LIGHT_SHF = 1;
    private final int FRONT_LIGHT_MSK = 1 << FRONT_LIGHT_SHF;
    private final int RESET_ACCUMULATED_CURRENT_SHF = 4;
    private final int RESET_ACCUMULATED_CURRENT_MSK = 1 << RESET_ACCUMULATED_CURRENT_SHF;
    private final int FAILSAFE_STOP_SHF = 5;
    private final int FAILSAFE_STOP_MSK = 1 << FAILSAFE_STOP_SHF;

    private int speed;
    private int steer;
    private int statusLed;
    private int frontLight;
    private int failSafeStop;
    private int resetAccCur;

    public SerialProtocolTx(){
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
        return  " VERSION      "+ VERSION +
                " LENGTH       "+ LENGTH +
                " speedVal     "+ speed+
                " steerVal     "+ steer+
                " statusLed    "+ statusLed+
                " frontLight   "+ frontLight+
                " failSafeStop "+ failSafeStop+
                " resetAccCur  "+ resetAccCur;
    }

    public synchronized byte[] get() {
        byte[] command = new byte[BUFFER_LENGTH];
        command[0] = START_BYTE;
        command[1] = getVersionLength(LENGTH);
        command[NUM_SPEED] = getSpeed();
        command[NUM_STEER] = getSteer();
        command[NUM_STATUS] = getStatus();
        command[NUM_CHECK] = getCheck(command, NUM_CHECK);
        return command;
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

    public synchronized byte getStatus(){
        return (byte) (0x00
                | ((statusLed << STATUS_LED_SHF) & STATUS_LED_MSK)
                | ((frontLight << FRONT_LIGHT_SHF) & FRONT_LIGHT_MSK)
                | ((failSafeStop << FAILSAFE_STOP_SHF) & FAILSAFE_STOP_MSK)
                | ((resetAccCur << RESET_ACCUMULATED_CURRENT_SHF) & RESET_ACCUMULATED_CURRENT_MSK));
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
