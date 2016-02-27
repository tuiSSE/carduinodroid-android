package tuisse.carduinodroid_android.data;


import org.json.JSONObject;

/**
 * Created by mate on 02.02.2016.
 */
public interface IpFrameIF {

    String parseJson(String jsonObjectRxData);
    JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus);

}
