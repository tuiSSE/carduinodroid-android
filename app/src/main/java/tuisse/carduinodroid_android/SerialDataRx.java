package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialDataRx extends SerialData{
    private final String TAG = "CarduinoSerialDataRx";

    private final int numCurrent = 2;
    private final int numAbsBattCap = 3;
    private final int numRelBattCap = 4;
    private final int numVoltage = 5;
    private final int numTemperature = 6;
    private final int numUltrasoundFront = 7;
    private final int numUltrasoundBack = 8;
    private final int numCheck = 9;

    //byte 0 Start
    //byte 1 Version+Length
    private final int length = 7;
    private final int bufferLength = length + bufferLengthOffset;
    private final int percentBatteryCapacityMask = 0xfe;
    private final int percentBatteryCapacityShift = 1;
    //byte 1
    private int current = 0;//in 0.1mA
    //byte 2
    private int absoluteBatteryCapacity = 0;//in 1mAh
    //byte 3
    private int percentBatteryCapacity = 0;//in %
    //byte 4
    private int voltage = 0;//in 0.1V
    //byte 5
    private int ds2745temperature = 0;//in 0.5°C
    //byte 6
    private int ultrasoundFront = 0;//in 1cm
    //byte 7
    private int ultrasoundBack = 0;//in 1cm

    private byte[] rxBuffer = new byte[bufferLength+1];
    private int    rxBufferLength = 0;

    public synchronized int getCurrent() {
        return current;
    }

    public synchronized int getAbsoluteBatteryCapacity() {
        return absoluteBatteryCapacity;
    }

    public synchronized int getPercentBatteryCapacity() {
        return percentBatteryCapacity;
    }

    public synchronized int getVoltage() {
        return voltage;
    }

    public synchronized int getDs2745Temperature() {
        return ds2745temperature;
    }

    public synchronized int getUltrasoundFront() {
        return ultrasoundFront;
    }

    public synchronized int getUltrasoundBack() {
        return ultrasoundBack;
    }

    public synchronized String print(){
        return  " current         " + current+
                " absCapacity     " + absoluteBatteryCapacity+
                " relCapacity     " + percentBatteryCapacity+
                " voltage         " + voltage+
                " temperature     " + ds2745temperature+
                " ultrasoundFront " + ultrasoundFront+
                " ultrasoundBack  " + ultrasoundBack;
    }

    public synchronized void append(byte inChar){
        if(inChar == startByte){
            rxBufferLength = 0;
        }

        //add char to buffer
        rxBuffer[rxBufferLength++] = inChar;
        //check if a full data packet was received

        if(rxBuffer[numStart] != startByte){
            rxBufferLength = 0;
            return;
        }
        if(rxBufferLength >= bufferLength) {
            if (rxBuffer[numVersionLength] != getVersionLength(length)) {
                rxBufferLength = 0;
                return;
            }
            if (rxBuffer[numCheck] != getCheck(rxBuffer, numCheck)) {
                rxBufferLength = 0;
                Log.e(TAG, "wrong Check byte on receive: " + rxBuffer[numCheck]+ " should be: " + getCheck(rxBuffer, numCheck));
                return;
            }
            //update values
            set(rxBuffer);
            rxBufferLength = 0;
        }
    }

    private synchronized void set(byte[] command) {
        if (command.length < bufferLength) {
            Log.e(TAG, "bufferLength out of bounds" + command.length);
            return;
        }
        if(command[numStart] != startByte){
            Log.e(TAG, "wrong Startbyte " + command[numStart]);
            return;
        }
        int recVersion = (command[numVersionLength] & versionMask) >> versionShift;
        int recLength = (command[numVersionLength] & lengthMask);
        if (((recVersion != version) || (recLength != length))) {
            Log.e(TAG, "wrong Version (" + recVersion + ") or bufferLength(" + recLength + ")");
            return;
        }
        //update values
        current = command[numCurrent];
        absoluteBatteryCapacity = command[numAbsBattCap];
        percentBatteryCapacity = (command[numRelBattCap] & percentBatteryCapacityMask) >> percentBatteryCapacityShift;
        voltage = command[numVoltage];
        ds2745temperature = command[numTemperature];
        ultrasoundFront = command[numUltrasoundFront];
        ultrasoundBack = command[numUltrasoundBack];
        Log.d(TAG, print());
    }
}
