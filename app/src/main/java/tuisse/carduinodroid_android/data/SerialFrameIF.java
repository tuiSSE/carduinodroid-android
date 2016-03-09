package tuisse.carduinodroid_android.data;

/**
 * Created by mate on 02.02.2016.
 */
public interface SerialFrameIF {

    /**
     * addembles a serial trame for tx direction
     * @return byte array conform to the serial protocol
     */
    byte[]  serialFrameAssembleTx();

    /**
     * appends an incoming byte inChar to the buffer and checks if a acomplete rx frame was received
     * @param inChar
     * @return true if a complete serial frame was received
     */
    boolean serialFrameAppendRx(byte inChar);
}
