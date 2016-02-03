package tuisse.carduinodroid_android.data;

/**
 * Created by mate on 02.02.2016.
 */
public interface SerialFrameIF {

    byte[]  serialFrameAssembleTx();
    boolean serialFrameAppendRx(byte inChar);
}
