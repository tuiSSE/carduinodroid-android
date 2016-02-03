package tuisse.carduinodroid_android.data;


import org.json.JSONObject;

/**
 * Created by mate on 02.02.2016.
 */
public interface IpFrameIF {

    boolean parseJson(JSONObject jsonObjectRxData);
    boolean createJsonObject(String dataTypeMask, String transmitData);
    JSONObject getTransmitData();

}
