package tuisse.carduinodroid_android;

import android.app.Application;

/**
 * Created by keX on 10.12.2015.
 */
public class DataContainer {
    protected Preferences preferences;
    protected SerialData serialData;
    protected IpData ipData;

    public DataContainer(){
        serialData = new SerialData();
        ipData = new IpData();
        preferences = new Preferences();
    }
}