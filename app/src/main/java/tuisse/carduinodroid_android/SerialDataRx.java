package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 07.12.2015.
 */
public class SerialDataRx {
    private final String TAG = "CarduinoSerialDataRx";
    private final int bufferLength = 8;
    //byte 0
    private final int version = 1;
    private final int length = bufferLength - 1;
    private final int versionShift = 6;
    private final int versionMask = 0xc0;
    private final int lengthMask = 0x0f;
    private final int percentBatteryCapacityMask = 0xfe;
    private final int percentBatteryCapacityShift = 1;
    //byte 1
    private int current = 0;
    //byte 2
    private int absoluteBatteryCapacity = 0;
    //byte 3
    private int percentBatteryCapacity = 0;
    //byte 4
    private int voltage = 0;
    //byte 5
    private int ds2745temperature = 0;
    //byte 6
    private int ultrasoundFront = 0;
    //byte 7
    private int ultrasoundBack = 0;

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

    public synchronized void set(byte[] command) {
        if (command.length != bufferLength) {
            Log.e(TAG, "bufferLength out of bounds" + command.length);
        } else {
            int recVersion = (command[0] & versionMask) >> versionShift;
            int recLength = (command[0] & lengthMask);
            if (((recVersion != version) || (recLength != length))) {
                Log.e(TAG, "wrong Version (" + recVersion + ") or bufferLength(" + recLength + ")");
            } else {
                current = command[1];
                absoluteBatteryCapacity = command[2];
                percentBatteryCapacity = (command[2] & percentBatteryCapacityMask) >> percentBatteryCapacityShift;
                voltage = command[4];
                ds2745temperature = command[5];
                ultrasoundFront = command[6];
                ultrasoundBack = command[7];
            }
        }
    }
}
