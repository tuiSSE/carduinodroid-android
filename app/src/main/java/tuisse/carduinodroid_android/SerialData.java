package tuisse.carduinodroid_android;

import android.app.Application;
import android.util.Log;

/**
 * Created by keX on 11.12.2015.
 */
abstract public class SerialData {
    private final String TAG = "CarduinoSerialData";
    protected CarduinodroidApplication carduino;

    //start byte
    //protected final int length;
    protected final byte startByte = (byte) 0x80;
    protected final int  version = 2;
    protected final int  versionShift = 4;
    protected final int  versionMask = 0x70;
    protected final int  lengthMask = 0x0f;
    protected final int  bufferLengthOffset = 3;
    protected final int  checkMask = 0x01;
    protected final int  parityBit = 7;
    protected final int  parityMask = 0x80;

    protected final int numStart = 0;
    protected final int numVersionLength = 1;

    public SerialData(Application a) {
        carduino = (CarduinodroidApplication) a;
    }


    public String byteArrayToHexString(byte[] array) {
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

    protected synchronized byte getVersionLength(int dataLength){
        if(version == 15) {
            Log.e(TAG, "version must not be 15");
            return (byte) 0x00;
        }
        return  (byte) (0x00 | ((dataLength) & lengthMask) | ((version << versionShift) & versionMask));
    }

    protected synchronized byte getCheck(byte[] cmd, int numCheck){
        if(numCheck+1 > cmd.length){
            Log.e(TAG, "get Check buffer length to small " + cmd.length + " it should at least be " + (numCheck+1));
            return (byte) 0x00;
        }
        byte check = 0x00;
        //calculate xor over all byte in frame
        for (int i = 0; i < numCheck; i++){
            check ^= cmd[i];
        }
        //calculate parity
        int parity = 0; //even parity
        for (int i = 0; i < parityBit; i++){
            if(((check >> i) & checkMask) == checkMask){
                parity ^= parityMask;
            }
        }
        check &= ~parityMask; //unset bit 7;
        check |= parity;//set parity bit
        return check;
    }
}
