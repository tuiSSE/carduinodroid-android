package tuisse.carduinodroid_android.data;


import org.json.JSONObject;

/**
* <h1>IP Frame Handler Interface</h1>
 * This Class is used for the preparation of all the transmission data between remote and
 * transceiver. One part contains the Creation of a certain transmission protocol based on JSON and
 * the other side is the parsing of received data. It provides the important functions or methods.
 *
 * @author Lars Vogel
 * @author Till Max Schwikal
 * @version 1.0
 * @since 03.02.2016
 */
public interface IpFrameIF {

    String parseJson(String jsonObjectRxData);
    JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus);

}
