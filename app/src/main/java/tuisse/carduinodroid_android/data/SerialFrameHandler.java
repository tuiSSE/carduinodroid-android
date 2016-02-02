package tuisse.carduinodroid_android.data;

import android.util.Log;

/**
 * Created by keX on 11.12.2015.
 */
public class SerialFrameHandler implements SerialFrameIF{
    private final String TAG = "CarduinoSerialFrame";

    private final int BUFFER_LENGTH_PROTOCOL_OFFSET = 3;

    private final int LENGTH_RX = 7;
    private final int BUFFER_LENGTH_RX = LENGTH_RX + BUFFER_LENGTH_PROTOCOL_OFFSET;
    private final int NUM_CURRENT = 2;
    private final int NUM_ABSOLUTE_BATTERY_CAPACITY = 3;
    private final int NUM_RELATIVE_BATTERY_CAPACITY = 4;
    private final int NUM_VOLTAGE = 5;
    private final int NUM_TEMPERATURE = 6;
    private final int NUM_ULTRASOUND_FRONT = 7;
    private final int NUM_ULTRASOUND_BACK = 8;
    private final int NUM_CHECK_RX = 9;

    private final int LENGTH_TX = 3;
    private final int BUFFER_LENGTH_TX = LENGTH_TX + BUFFER_LENGTH_PROTOCOL_OFFSET;
    private final int NUM_SPEED = 2;
    private final int NUM_STEER = 3;
    private final int NUM_STATUS = 4;
    private final int NUM_CHECK_TX = 5;

    private final int STATUS_LED_SHF = 0;
    private final int STATUS_LED_MSK = 1 << STATUS_LED_SHF;
    private final int FRONT_LIGHT_SHF = 1;
    private final int FRONT_LIGHT_MSK = 1 << FRONT_LIGHT_SHF;
    private final int RESET_ACCUMULATED_CURRENT_SHF = 4;
    private final int RESET_ACCUMULATED_CURRENT_MSK = 1 << RESET_ACCUMULATED_CURRENT_SHF;
    private final int FAILSAFE_STOP_SHF = 5;
    private final int FAILSAFE_STOP_MSK = 1 << FAILSAFE_STOP_SHF;

    //start byte
    private final byte START_BYTE = (byte) 0x80;
    private final int VERSION = 2;
    private final int VERSION_FORBIDDEN = 15;
    private final int VERSION_SHF = 4;
    private final int VERSION_MSK = 0x70;
    private final int LENGTH_MSK = 0x0f;
    private final int CHECK_MSK = 0x01;
    private final int PARITY_BIT = 7;
    private final int PARITY_MSK = 1 << PARITY_BIT;

    private final int NUM_START = 0;
    private final int NUM_VERSION_LENGTH = 1;
    private final int BYTE_MASK = 0xff;

    private CarduinoData carduinoData;
    private byte[] rxBuffer = new byte[BUFFER_LENGTH_RX +1];
    private int    rxBufferLength = 0;

    public SerialFrameHandler(CarduinoData cd){
        carduinoData = cd;
    }

    public synchronized byte[] serialFrameAssembleTx() {
        byte[] command = new byte[BUFFER_LENGTH_TX];
        command[NUM_START] = START_BYTE;
        command[NUM_VERSION_LENGTH] = getVersionLength(LENGTH_TX);
        command[NUM_SPEED] = carduinoData.getSpeed();
        command[NUM_STEER] = carduinoData.getSteer();
        command[NUM_STATUS] = getStatusByte();
        command[NUM_CHECK_TX] = getCheck(command, NUM_CHECK_TX);
        return command;
    }

    public synchronized boolean serialFrameAppendRx(byte inChar){
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
        if(rxBufferLength >= BUFFER_LENGTH_RX) {
            if (rxBuffer[NUM_VERSION_LENGTH] != getVersionLength(LENGTH_RX)) {
                rxBufferLength = 0;
                return false;
            }
            if (rxBuffer[NUM_CHECK_RX] != getCheck(rxBuffer, NUM_CHECK_RX)) {
                rxBufferLength = 0;
                Log.e(TAG, "wrong Check byte on receive: 0x" + Utils.byteToHexString(rxBuffer[NUM_CHECK_RX])+ " calculated: 0x" + Utils.byteToHexString(getCheck(rxBuffer, NUM_CHECK_RX)) + " rx Buffer: " + Utils.byteArrayToHexString(rxBuffer));
                return false;
            }
            //update values
            rxBufferLength = 0;
            return set(rxBuffer);
        }
        return false;
    }

    private synchronized boolean set(byte[] command) {
        if (command.length < BUFFER_LENGTH_RX) {
            Log.e(TAG, "BUFFER_LENGTH out of bounds" + command.length);
            return false;
        }
        if (command[NUM_START] != START_BYTE){
            Log.e(TAG, "wrong Startbyte " + Utils.byteToHexString(command[NUM_START]));
            return false;
        }
        int recVersion = (command[NUM_VERSION_LENGTH] & VERSION_MSK) >> VERSION_SHF;
        int recLength = (command[NUM_VERSION_LENGTH] & LENGTH_MSK);
        if (((recVersion != VERSION) || (recLength != LENGTH_RX))) {
            Log.e(TAG, "wrong Version (" + recVersion + ") or BUFFER_LENGTH(" + recLength + ")");
            return false;
        }
        //update values
        carduinoData.setCurrent(command[NUM_CURRENT] & BYTE_MASK);
        carduinoData.setAbsBattCap(command[NUM_ABSOLUTE_BATTERY_CAPACITY] & BYTE_MASK);
        carduinoData.setRelBattCap(command[NUM_RELATIVE_BATTERY_CAPACITY]  & BYTE_MASK);
        carduinoData.setVoltage(command[NUM_VOLTAGE] & BYTE_MASK);
        carduinoData.setTemperature(command[NUM_TEMPERATURE] & BYTE_MASK);
        carduinoData.setUltrasoundFront(command[NUM_ULTRASOUND_FRONT] & BYTE_MASK);
        carduinoData.setUltrasoundBack(command[NUM_ULTRASOUND_BACK] & BYTE_MASK);
        Log.d(TAG, carduinoData.print());
        return true;
    }

    private synchronized byte getStatusByte(){
        return (byte) (0x00
                | ((carduinoData.getStatusLed() << STATUS_LED_SHF) & STATUS_LED_MSK)
                | ((carduinoData.getFrontLight() << FRONT_LIGHT_SHF) & FRONT_LIGHT_MSK)
                | ((carduinoData.getFailSafeStop() << FAILSAFE_STOP_SHF) & FAILSAFE_STOP_MSK)
                | ((carduinoData.getResetAccCur() << RESET_ACCUMULATED_CURRENT_SHF) & RESET_ACCUMULATED_CURRENT_MSK));
    }

    private synchronized byte getVersionLength(int dataLength){
        if(VERSION == VERSION_FORBIDDEN) {
            //this check just verifies if the version number is allowed.
            //it should never occur, since VERSION != VERSION_FORBIDDEN = 15!
            Log.e(TAG, "VERSION must not be VERSION_FORBIDDEN(" + VERSION_FORBIDDEN+ ")");
            return (byte) 0x00;
        }
        return  (byte) (0x00 | ((dataLength) & LENGTH_MSK) | ((VERSION << VERSION_SHF) & VERSION_MSK));
    }

    private synchronized byte getCheck(byte[] cmd, int numCheck){
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
