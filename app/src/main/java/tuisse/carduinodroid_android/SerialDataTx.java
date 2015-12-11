package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialDataTx {
    private final String TAG = "CarduinoSerialDataTx";
    private final int bufferLength = 4;
    //byte 0
    private final int version = 1;
    private final int length = bufferLength - 1;
    private final int versionShift = 6;
    private final int versionMask = 0xc0;
    private final int lengthMask = 0x0f;
    private final int speedValShift = 4;
    private final int speedValMask = 0xf0;
    private final int speedValMax = 9;
    private final int speedValMin = 0;
    private final int speedDirShift = 3;
    private final int speedDirMask = 0x08;
    private final int steerValShift = 4;
    private final int steerValMask = 0xf0;
    private final int steerValMax = 0x8;
    private final int steerValMin = 0x0;
    private final int steerDirShift = 3;
    private final int steerDirMask = 0x08;
    private final int statusLedShift = 7;
    private final int statusLedMask = 0x80;
    private final int frontLightShift = 6;
    private final int frontLightMask = 0x40;
    private final int failSafeStopShift = 5;
    private final int failSafeStopMask = 0x20;
    private final int resetAccCurShift = 4;
    private final int resetAccCurMask = 0x10;
    //byte 1
    private int speedVal;
    private int speedDir;
    //byte 2
    private int steerVal;
    private int steerDir;
    //byte 3
    private int statusLed;
    private int frontLight;
    private int failSafeStop;
    private int resetAccCur;

    public SerialDataTx(){
        speedVal = 4;
        speedDir = 1;
        //byte 2
        steerVal = 0;
        steerDir = 0;
        //byte 3
        statusLed = 0;
        frontLight = 1;
        failSafeStop = 1;
        resetAccCur = 0;
    }

    public synchronized static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
            hexString.append(" ");
        }
        return hexString.toString();
    }

    public synchronized String print(){
        return  "version      "+ version+
                "length       "+ length+
                "speedVal     "+ speedVal+
                "speedDir     "+ speedDir+
                "steerVal     "+ steerVal+
                "steerDir     "+ steerDir+
                "statusLed    "+ statusLed+
                "frontLight   "+ frontLight+
                "failSafeStop "+ failSafeStop+
                "resetAccCur  "+ resetAccCur;
    }

    public synchronized byte[] get() {
        byte[] command = new byte[bufferLength];
        command[0] = (byte) (0x00 | ((length) & lengthMask)
                | ((version << versionShift) & versionMask));
        command[1] = (byte) (0x00 | ((speedDir << speedDirShift) & speedDirMask)
                | ((speedVal << speedValShift) & speedValMask));
        command[2] = (byte) (0x00 | ((steerDir << steerDirShift) & steerDirMask)
                | ((steerVal << steerValShift) & steerValMask));
        command[3] = (byte) (0x00 | ((statusLed << statusLedShift) & statusLedMask)
                | ((frontLight << frontLightShift) & frontLightMask)
                | ((failSafeStop << failSafeStopShift) & failSafeStopMask)
                | ((resetAccCur << resetAccCurShift) & resetAccCurMask));
        //Log.d(TAG,print());
        //Log.d(TAG,byteArrayToHexString(command));
        return command;
    }

    public synchronized void setSpeed(int speed) {
        if ((speed < -speedValMax) || (speed > speedValMax))
            Log.e(TAG, "setSpeed out of bounds" + speed);
        else {
            if (speed < speedValMin) {
                this.speedDir = 0; //backwards
                this.speedVal = -speed; // speed is negative
            } else {
                this.speedDir = 1; //forwards
                this.speedVal = speed;
            }
        }
    }

    public synchronized void setSpeedVal(int speedVal) {
        if ((speedVal >= speedValMin) && (speedVal <= speedValMax))
            this.speedVal = speedVal;
        else
            Log.e(TAG, "setSpeedVal out of bounds" + speedVal);
    }

    public synchronized void setSpeedDir(int speedDir) {
        if (speedDir != 0)
            this.speedDir = 1;//forwards
        else
            this.speedDir = 0;//backwards
    }

    public synchronized void setSteer(int steer) {
        if ((steer < -steerValMax) || (steer > steerValMax))
            Log.e(TAG, "setSteer out of bounds" + steer);
        else {
            if (steer < steerValMin) {
                this.steerDir = 0; //left
                this.steerVal = -steer; // steer is negative
            } else {
                this.steerDir = 1; //right
                this.steerVal = steer;
            }
        }
    }

    public synchronized void setSteerVal(int steerVal) {
        if ((steerVal >= steerValMin) && (steerVal <= steerValMax))
            this.steerVal = steerVal;
        else
            Log.e(TAG, "setSteerVal out of bounds" + steerVal);
    }

    public synchronized void setSteerDir(int steerDir) {
        if (steerDir != 0)
            this.steerDir = 1;//right
        else
            this.steerDir = 0;//left
    }

    public synchronized void setStatusLed(int statusLed) {
        if (statusLed != 0)
            this.statusLed = 1;//on
        else
            this.statusLed = 0;//off = default
    }

    public synchronized void setFrontLight(int frontLight) {
        if (frontLight != 0)
            this.frontLight = 1;//on
        else
            this.frontLight = 0;//off = default
    }

    public synchronized void setFailSafeStop(int failSafeStop) {
        if (failSafeStop != 0)
            this.failSafeStop = 1;//on = default
        else
            this.failSafeStop = 0;//off
    }

    public synchronized void setResetAccCur(int resetAccCur) {
        if (resetAccCur != 0)
            this.resetAccCur = 1;//on
        else
            this.resetAccCur = 0;//off = default
    }
}
