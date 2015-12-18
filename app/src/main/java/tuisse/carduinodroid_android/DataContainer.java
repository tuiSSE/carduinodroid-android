package tuisse.carduinodroid_android;

import android.app.Application;

/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer {
    protected CarduinodroidApplication carduino;

    public Preferences preferences;
    public SerialDataRx serialDataRx;
    public SerialDataTx serialDataTx;

    public DataContainer(Application a){
        carduino = (CarduinodroidApplication) a;
        serialDataRx = new SerialDataRx(a);
        serialDataTx = new SerialDataTx(a);
    }
}