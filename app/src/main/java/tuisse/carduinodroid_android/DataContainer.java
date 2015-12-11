package tuisse.carduinodroid_android;

/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer {
    public Preferences preferences;
    public SerialDataRx serialDataRx;
    public SerialDataTx serialDataTx;

    public DataContainer(){
        serialDataRx = new SerialDataRx();
        serialDataTx = new SerialDataTx();
    }
}