package tuisse.carduinodroid_android;
import android.content.Intent;
import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialProtocolRx extends SerialProtocol {
    private final String TAG = "CarduinoSerialRx";

    private final int NUM_CURRENT = 2;
    private final int NUM_ABSOLUTE_BATTERY_CAPACITY = 3;
    private final int NUM_RELATIVE_BATTERY_CAPACITY = 4;
    private final int NUM_VOLTAGE = 5;
    private final int NUM_TEMPERATURE = 6;
    private final int NUM_ULTRASOUND_FRONT = 7;
    private final int NUM_ULTRASOUND_BACK = 8;
    private final int NUM_CHECK = 9;

    private final int BYTE_MASK = 0xff;

    //byte 0 Start
    //byte 1 Version+Length
    private final int LENGTH = 7;
    private final int BUFFER_LENGTH = LENGTH + BUFFER_LENGTH_PROTOCOL_OFFSET;
    //byte 1
    private int current;//in 0.1mA
    //byte 2
    private int absoluteBatteryCapacity;//in 1mAh
    //byte 3
    private int percentBatteryCapacity;//in %
    //byte 4
    private int voltage;//in 0.1V
    //byte 5
    private int ds2745temperature;//in 0.5°C
    //byte 6
    private int ultrasoundFront;//in 1cm
    //byte 7
    private int ultrasoundBack;//in 1cm

    private byte[] rxBuffer = new byte[BUFFER_LENGTH +1];
    private int    rxBufferLength = 0;

    public SerialProtocolRx() {
        reset();
    }

    public synchronized void reset(){
        current = 0;
        absoluteBatteryCapacity = 0;
        percentBatteryCapacity = 0;
        voltage = 0;
        ds2745temperature = 0;
        ultrasoundFront = 0;
        ultrasoundBack = 0;
    }

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

    public synchronized boolean append(byte inChar){
        if(inChar == START_BYTE){
            rxBufferLength = 0;
        }
        //add char to buffer
        rxBuffer[rxBufferLength++] = inChar;
        //check if a full data packet was received

        if(rxBuffer[NUM_START] != START_BYTE){
            rxBufferLength = 0;
            return false;
        }
        if(rxBufferLength >= BUFFER_LENGTH) {
            if (rxBuffer[NUM_VERSION_LENGTH] != getVersionLength(LENGTH)) {
                rxBufferLength = 0;
                return false;
            }
            if (rxBuffer[NUM_CHECK] != getCheck(rxBuffer, NUM_CHECK)) {
                rxBufferLength = 0;
                Log.e(TAG, "wrong Check byte on receive: 0x" + byteToHexString(rxBuffer[NUM_CHECK])+ " calculated: 0x" + byteToHexString(getCheck(rxBuffer, NUM_CHECK)));
                return false;
            }
            //update values
            rxBufferLength = 0;
            return set(rxBuffer);
        }
        return false;
    }

    private synchronized boolean set(byte[] command) {
        if (command.length < BUFFER_LENGTH) {
            Log.e(TAG, "BUFFER_LENGTH out of bounds" + command.length);
            return false;
        }
        if(command[NUM_START] != START_BYTE){
            Log.e(TAG, "wrong Startbyte " + byteToHexString(command[NUM_START]));
            return false;
        }
        int recVersion = (command[NUM_VERSION_LENGTH] & VERSION_MSK) >> VERSION_SHF;
        int recLength = (command[NUM_VERSION_LENGTH] & LENGTH_MSK);
        if (((recVersion != VERSION) || (recLength != LENGTH))) {
            Log.e(TAG, "wrong Version (" + recVersion + ") or BUFFER_LENGTH(" + recLength + ")");
            return false;
        }
        //update values
        current = command[NUM_CURRENT] & BYTE_MASK;
        absoluteBatteryCapacity = command[NUM_ABSOLUTE_BATTERY_CAPACITY] & BYTE_MASK;
        percentBatteryCapacity = command[NUM_RELATIVE_BATTERY_CAPACITY]  & BYTE_MASK;
        voltage = command[NUM_VOLTAGE] & BYTE_MASK;
        ds2745temperature = command[NUM_TEMPERATURE] & BYTE_MASK;
        ultrasoundFront = command[NUM_ULTRASOUND_FRONT] & BYTE_MASK;
        ultrasoundBack = command[NUM_ULTRASOUND_BACK] & BYTE_MASK;
        if (ultrasoundBack == BYTE_MASK) ultrasoundBack = -1;
        if (ultrasoundFront == BYTE_MASK) ultrasoundFront = -1;
        //Log.d(TAG, print());
        return true;
    }
}
