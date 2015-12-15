package tuisse.carduinodroid_android;

import android.util.Log;

/**
 * Created by keX on 11.12.2015.
 */
public class SerialData {
    private final String TAG = "CarduinoSerialData";
    //start byte
    protected final byte startByte = (byte) 0x80;
    protected final int  bufferLengthOffset = 3;
    protected final int  checkMask = 0x01;
    protected final int  parityBit = 7;
    protected final int  parityMask = 0x80;

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

    public synchronized byte getCheck(byte[] cmd){
        int length = cmd.length;
        if(length < bufferLengthOffset){
            Log.e(TAG, "get Check buffer length to small" + length);
            return (byte) 0x00;
        }
        byte check = 0x00;
        //calculate xor over all byte in frame
        for (int i = 0; i < length -1; i++){
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
