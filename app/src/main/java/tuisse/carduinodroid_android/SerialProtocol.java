package tuisse.carduinodroid_android;

import android.app.Application;
import android.util.Log;

/**
 * Created by keX on 11.12.2015.
 */
abstract public class SerialProtocol {
    private final String TAG = "CarduinoSerialProtocol";

    //start byte
    protected final byte START_BYTE = (byte) 0x80;
    protected final int VERSION = 2;
    protected final int VERSION_FORBIDDEN = 15;
    protected final int VERSION_SHF = 4;
    protected final int VERSION_MSK = 0x70;
    protected final int LENGTH_MSK = 0x0f;
    protected final int BUFFER_LENGTH_PROTOCOL_OFFSET = 3;
    protected final int CHECK_MSK = 0x01;
    protected final int PARITY_BIT = 7;
    protected final int PARITY_MSK = 1 << PARITY_BIT;

    protected final int NUM_START = 0;
    protected final int NUM_VERSION_LENGTH = 1;

    public synchronized String byteArrayToHexString(byte[] array) {
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
        if(VERSION == VERSION_FORBIDDEN) {
            //this check just verifies if the version number is allowed.
            //it should never occur, since VERSION != VERSION_FORBIDDEN = 15!
            Log.e(TAG, "VERSION must not be VERSION_FORBIDDEN(" + VERSION_FORBIDDEN+ ")");
            return (byte) 0x00;
        }
        return  (byte) (0x00 | ((dataLength) & LENGTH_MSK) | ((VERSION << VERSION_SHF) & VERSION_MSK));
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
        for (int i = 0; i < PARITY_BIT; i++){
            if(((check >> i) & CHECK_MSK) == CHECK_MSK){
                parity ^= PARITY_MSK;
            }
        }
        check &= ~PARITY_MSK; //unset bit 7;
        check |= parity;//set parity bit
        return check;
    }
}
