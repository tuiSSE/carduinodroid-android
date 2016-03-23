package tuisse.carduinodroid_android.data;


/**
 * <h1>Serial frame handler class</h1>
 * class which assembles and disassembles serial frames. lots of constants describe how a frame is assembled and at which position a particular byte is located
 *
 * @author Till Max Schwikal
 * @since 02.02.2016
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.data.SerialFrameHandler
 * @see tuisse.carduinodroid_android.data.DataHandler
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
