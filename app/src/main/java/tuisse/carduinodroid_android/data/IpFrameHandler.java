package tuisse.carduinodroid_android.data;

import android.util.Log;

/**
 * Created by mate on 02.02.2016.
 */
public class IpFrameHandler implements IpFrameIF{
    private final String TAG = "CarduinoIpFrame";
    private CarduinoDroidData carduinoDroidData;


    public IpFrameHandler(CarduinoDroidData cdd){
        carduinoDroidData = cdd;
    }
}
