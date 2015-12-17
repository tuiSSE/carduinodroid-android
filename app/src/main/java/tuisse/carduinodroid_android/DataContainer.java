package tuisse.carduinodroid_android;

import android.app.Application;

/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer {
    protected CarduinodroidApplication carduino;

    public IntentStrings intentStrings;
    public Preferences preferences;
    public SerialDataRx serialDataRx;
    public SerialDataTx serialDataTx;

    public DataContainer(Application a){
        carduino = (CarduinodroidApplication) a;
        intentStrings = new IntentStrings();
        serialDataRx = new SerialDataRx(a);
        serialDataTx = new SerialDataTx(a);
    }
}