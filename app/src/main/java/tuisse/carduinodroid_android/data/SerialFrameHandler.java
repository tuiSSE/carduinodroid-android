package tuisse.carduinodroid_android.data;

import android.util.Log;

import tuisse.carduinodroid_android.Constants;

/**
 * <h1>Serial frame handler class</h1>
 * class which assembles and disassembles serial frames. lots of constants describe how a frame is assembled and at which position a particular byte is located
 *
 * @author Till Max Schwikal
 * @since 11.12.2015
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.data.SerialFrameIF
 * @see tuisse.carduinodroid_android.data.CarduinoData
 * @see tuisse.carduinodroid_android.data.DataHandler
 */
public class SerialFrameHandler implements SerialFrameIF{
    private final String TAG = "CarduinoSerialFrame";

    private final int BUFFER_LENGTH_PROTOCOL_OFFSET = 3;///< number of bytes of the protocol header and tail

    private final int LENGTH_RX = 7;///< number of information bytes in rx
    private final int BUFFER_LENGTH_RX = LENGTH_RX + BUFFER_LENGTH_PROTOCOL_OFFSET;///< total frame length in rx
    private final int NUM_CURRENT = 2;///< position of the current byte
    private final int NUM_ABSOLUTE_BATTERY_CAPACITY = 3;///< position of the absolute battery capacity byte
    private final int NUM_RELATIVE_BATTERY_CAPACITY = 4;///< position of the relative battery capacity byte
    private final int NUM_VOLTAGE = 5;///< position of the voltage byte
    private final int NUM_TEMPERATURE = 6;///< position of the temperature byte
    private final int NUM_ULTRASOUND_FRONT = 7;///< position of the ultrasound front distance byte
    private final int NUM_ULTRASOUND_BACK = 8;///< position of the ultrasound back distance byte
    private final int NUM_CHECK_RX = 9;///< position of the check byte in rx (not part of the information)

    private final int LENGTH_TX = 3;///< number of information byte in tx
    private final int BUFFER_LENGTH_TX = LENGTH_TX + BUFFER_LENGTH_PROTOCOL_OFFSET;///< total frame length in tx
    private final int NUM_SPEED = 2;///< position of the speed byte
    private final int NUM_STEER = 3;///< position of the steer byte
    private final int NUM_STATUS = 4;///< position of the status byte
    private final int NUM_CHECK_TX = 5;///< position of the check byte in tx (not part of the information)

    private final int STATUS_LED_SHF = 0;///< status led position within status byte
    private final int STATUS_LED_MSK = 1 << STATUS_LED_SHF;///< mask of the status led within status byte
    private final int FRONT_LIGHT_SHF = 1;///< front led position within status byte
    private final int FRONT_LIGHT_MSK = 1 << FRONT_LIGHT_SHF;///< mask of the front light within status byte
    private final int RESET_ACCUMULATED_CURRENT_SHF = 4;///< reset accumulated current bit position within status byte
    private final int RESET_ACCUMULATED_CURRENT_MSK = 1 << RESET_ACCUMULATED_CURRENT_SHF;///< mask of the reset accumulated current bit within status byte
    private final int FAILSAFE_STOP_SHF = 5;///< fail safe stop position within status byte
    private final int FAILSAFE_STOP_MSK = 1 << FAILSAFE_STOP_SHF;///< mask of the fail safe stop bit within status byte

    //start byte
    private final byte START_BYTE = (byte) 0x80;///< start byte
    private final int VERSION = 2;///< serial protocol version number
    private final int VERSION_FORBIDDEN = 8;///< forbidden version number due to start byte side effects
    private final int VERSION_SHF = 4;///< version shift
    private final int VERSION_MSK = 0x70;///< version mask, makes shure version is between 0-7
    private final int LENGTH_MSK = 0x0f;///< length of serial frame mask
    private final int CHECK_MSK = 0x01;///< check mask needed for calculating and evaluating the check byte
    private final int PARITY_BIT = 7;///< position of the parity bit
    private final int PARITY_MSK = 1 << PARITY_BIT;///< mask of the parity bit

    private final int NUM_START = 0;///< position of the start byte in every frame
    private final int NUM_VERSION_LENGTH = 1;///< position of the version & length byte in every frame
    private final int BYTE_MASK = 0xff;///< mask for a byte

    private CarduinoData carduinoData;///< local connection to the carduino database
    private byte[] rxBuffer = new byte[BUFFER_LENGTH_RX +1];///< rx buffer
    private int    rxBufferLength = 0;///< current serial buffer length (incremented if a valid byte is appended)

    /**
     * constructor of the serial frame handler
     * @param cd carduino database
     */
    public SerialFrameHandler(CarduinoData cd){
        carduinoData = cd;
    }

    /**
     * function to create a serial frame for tx direction
     * @return byte array to be sent
     */
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

    /**
     * function to append a read byte inChar to the serial read buffer
     * @param inChar new byte which should be appended
     * @return true if after the last appended byte a valid serial frame was received
     */
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
            return serialFrameCheck(rxBuffer);
        }
        return false;
    }

    /**
     * checks a serial frame on validity
     * @param command byte array of the current received serial command
     * @return true if the command is a valid command
     */
    private synchronized boolean serialFrameCheck(byte[] command) {
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
        carduinoData.setRelBattCap(command[NUM_RELATIVE_BATTERY_CAPACITY] & BYTE_MASK);
        carduinoData.setVoltage(command[NUM_VOLTAGE] & BYTE_MASK);
        carduinoData.setTemperature(command[NUM_TEMPERATURE] & BYTE_MASK);
        carduinoData.setUltrasoundFront(command[NUM_ULTRASOUND_FRONT] & BYTE_MASK);
        carduinoData.setUltrasoundBack(command[NUM_ULTRASOUND_BACK] & BYTE_MASK);
        if(Constants.LOG.SERIAL_RECEIVER){
            Log.d(TAG, carduinoData.print());
        }
        return true;
    }


    /**
     * function which assembles the status byte from several bits
     * @return status byte
     */
    private synchronized byte getStatusByte(){
        return (byte) (
                  ((carduinoData.getStatusLed()    << STATUS_LED_SHF) & STATUS_LED_MSK)
                | ((carduinoData.getFrontLight()   << FRONT_LIGHT_SHF) & FRONT_LIGHT_MSK)
                | ((carduinoData.getFailSafeStop() << FAILSAFE_STOP_SHF) & FAILSAFE_STOP_MSK)
                | ((carduinoData.getResetAccCur()  << RESET_ACCUMULATED_CURRENT_SHF) & RESET_ACCUMULATED_CURRENT_MSK));
    }

    /**
     * function which assembles the version length byte
     * @param dataLength length of the current serial frame
     * @return version and length byte
     */
    private synchronized byte getVersionLength(int dataLength){
        if(VERSION >= VERSION_FORBIDDEN) {
            //this check just verifies if the version number is allowed.
            //it should never occur, since VERSION < VERSION_FORBIDDEN = 8!
            Log.e(TAG, "VERSION("+VERSION+") must not be higher than VERSION_FORBIDDEN(" + VERSION_FORBIDDEN+ ")");
            return (byte) 0x00;
        }
        return  (byte) (0x00 | ((dataLength) & LENGTH_MSK) | ((VERSION << VERSION_SHF) & VERSION_MSK));
    }

    /**
     * function which calculates the check byte
     * @param cmd byte array command without check byte but enough space for it
     * @param numCheck position of the check byte within the command
     * @return calculated check byte dependent on the command
     */
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
