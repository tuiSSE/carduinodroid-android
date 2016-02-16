package tuisse.carduinodroid_android.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import tuisse.carduinodroid_android.Constants;

/**
 * Created by mate on 02.02.2016.
 */
public class IpFrameHandler implements IpFrameIF{
    private final String TAG = "CarduinoIpFrame";
    private CarduinoDroidData carduinoDroidData;
    private CarduinoData carduinoData;

    private JSONObject JsonObjectData;
    private JSONObject JsonObjectFalse;
    private boolean isMaskTypeServer;

    private boolean isForClient;
    private boolean isForServer;

    public IpFrameHandler(CarduinoData cd, CarduinoDroidData cdd){
        carduinoDroidData = cdd;
        carduinoData = cd;
        //setClientVersion(0);
    }

    // translate a received JSON object to get all the send information out of it and set it to
    // their variables
    public synchronized boolean parseJson(String jsonObjectRxData) {
        // return isConnectedRunning = true if Data Server is already connected/running or if there
        // are some errors occurring
        try {
            JSONObject jsonObject = new JSONObject(jsonObjectRxData) ;

            JSONObject JsonObjectHeader = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_HEADER);
            //First Check if both sides (Remote & Transceiver) using the same JSON Version
            if(JsonObjectHeader.getInt(Constants.JSON_OBJECT.TAG_HEADER_VERSION) != Constants.JSON_OBJECT.MY_VERSION) {
                Log.e(TAG, "Wrong JSON Version between Transmitter and Receiver");
                return true;
            }else{
                String mask = JsonObjectHeader.getString(Constants.JSON_OBJECT.TAG_HEADER_INFORMATION_TYPE);
                if(isMaskLogic(mask)){
                    //Define the Type for the right Parsing
                    if(!isForServer&&!isForClient){
                        //Ctrl Server Socket Data: Information about Data Server Socket Status as feedback
                        Log.i(TAG, String.valueOf(JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS)));
                        return JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS);
                    }else if(isForClient){
                        //Parsing all the information given from a Server to the Client

                        return true;
                    }else{
                        //Parsing all the information given from a Client to the Server

                        return true;
                    }
                }else{
                    Log.e(TAG, "Error on JSON Parsing by mixed up Data inclusion (Both Client and Server Data)");
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean isMaskLogic(String mask){

        boolean isCar = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAR);
        boolean isMobility = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_MOBILITY);
        boolean isNetwork = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_NETWORK);
        boolean isHardware = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_HARDWARE);
        boolean isVideo = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_VIDEO);
        boolean isControl = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CONTROL);
        boolean isCamera = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAMERA);
        boolean isSound = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_SOUND);

        isForClient = (isCar || isMobility || isNetwork || isHardware || isVideo);
        isForServer = (isControl || isCamera ||isSound);

        return !(isForClient&&isForServer);
    }

    // Standard Build of the transmitted JSON Object defined by Header, Hardware information,
    // Car information, vibration and camera resolution out of the available variables
    private synchronized boolean createJsonObject(String dataTypeMask, boolean dataServerStatus) {

        isMaskTypeServer = false;

        try {
            JsonObjectData = new JSONObject();
            // Header is used for all the JSON Objects to define Version and Type
            // It should be in Client and Server Packet to have a certain control setup
            JSONObject JsonObjectHeader = new JSONObject();

            // Create the Header to define version, data type to secure the transmission and Ctrl Socket
            JsonObjectHeader.put(Constants.JSON_OBJECT.TAG_HEADER_VERSION, Constants.JSON_OBJECT.MY_VERSION);
            JsonObjectHeader.put(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS, dataServerStatus);
            JsonObjectHeader.put(Constants.JSON_OBJECT.TAG_HEADER_INFORMATION_TYPE, dataTypeMask);
            JsonObjectData.put(Constants.JSON_OBJECT.TAG_HEADER, JsonObjectHeader);

            /**** Starting with Transceiver/Server Paket Part ****/
            /**** Including Car Information ****/
            // often updated information base
            // Collect all the Car information for the JSON Object to the remote side
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_CAR)) {

                JSONObject JsonObjectCarInformation = new JSONObject();

                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_CURRENT, carduinoData.getCurrent());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_BATTERY_ABSOLUTE, carduinoData.getAbsBattCap());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_BATTERY_PERCENTAGE, carduinoData.getRelBattCap());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_VOLTAGE, carduinoData.getVoltage());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_TEMPERATURE, carduinoData.getTemperature());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_FRONT, carduinoData.getUltrasoundFront());
                JsonObjectCarInformation.put(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_BACK, carduinoData.getUltrasoundBack());

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_CAR, JsonObjectCarInformation);
                isMaskTypeServer = true;
            }
            /**** Including Mobility Information ****/
            // Collect all the mobility information (GPS,Vibration) for the JSON Object to the
            // remote side
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_MOBILITY)) {

                JSONObject JsonObjectMobilityInformation = new JSONObject();

                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_GPS, carduinoDroidData.getGpsData());
                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_VIBRATION, carduinoDroidData.getVibration());

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_MOBILITY, JsonObjectMobilityInformation);
                isMaskTypeServer = true;
            }
            /**** Including Video Data ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_VIDEO)) {
                JSONObject JsonObjectVideoData = new JSONObject();

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_NETWORK, JsonObjectVideoData);
                isMaskTypeServer = true;
            }
            /**** Including Network Information ****/
            // rarely updated information based on changed values like
            // Collect all the network information for the JSON Object to the remote side
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_NETWORK)) {

                JSONObject JsonObjectNetworkInformation = new JSONObject();

                JsonObjectNetworkInformation.put(Constants.JSON_OBJECT.TAG_NETWORK_MOBILE_AVAILABLE, carduinoDroidData.getMobileAvailable());
                JsonObjectNetworkInformation.put(Constants.JSON_OBJECT.TAG_NETWORK_MOBILE_ACTIVE, carduinoDroidData.getMobileActive());
                JsonObjectNetworkInformation.put(Constants.JSON_OBJECT.TAG_NETWORK_WLAN_AVAILABLE, carduinoDroidData.getWLANAvailable());
                JsonObjectNetworkInformation.put(Constants.JSON_OBJECT.TAG_NETWORK_WLAN_ACTIVE, carduinoDroidData.getWLANActive());

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_NETWORK, JsonObjectNetworkInformation);
                isMaskTypeServer = true;
            }
            /**** Including Hardware Information ****/
            // once collected information based on a certain mobile phone setup
            // Collect all the hardware feature based on the mobile phone for the JSON Object
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_HARDWARE)) {

                JSONObject JsonObjectHardwareInformation = new JSONObject();
                //TO-DO
                JsonObjectHardwareInformation.put(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION, carduinoDroidData.getCameraResolution());

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_HARDWARE, JsonObjectHardwareInformation);
                isMaskTypeServer = true;
            }

            /**** Starting with Client Paket Part ****/
            /**** Including Car Control ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_CONTROL)) {

                if(isMaskTypeServer == true) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else{
                    JSONObject JsonObjectCarControl = new JSONObject();
                    // TO-DO
                    JsonObjectData.put(Constants.JSON_OBJECT.TAG_CONTROL, JsonObjectCarControl);
                }
            }
            /**** Including Camera Information ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_CAMERA)) {
                if(isMaskTypeServer == true) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else {
                    JSONObject JsonObjectCameraInformation = new JSONObject();
                    // TO-DO
                    JsonObjectData.put(Constants.JSON_OBJECT.TAG_CAMERA, JsonObjectCameraInformation);
                }
            }
            /**** Including Sound Options ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_SOUND)) {
                if(isMaskTypeServer == true) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else {
                    JSONObject JsonObjectSoundOptions = new JSONObject();
                    // TO-DO
                    JsonObjectData.put(Constants.JSON_OBJECT.TAG_SOUND, JsonObjectSoundOptions);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Return the last created JSON Object with the current variables
    public synchronized JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus){

        if(createJsonObject(dataTypeMask, dataServerStatus))
            return JsonObjectData;
        else return null;
    }

    private synchronized boolean checkDataTypeMask(String dataTypeMask, String Type){

        if(dataTypeMask.contains(Type)){
            return true;
        }else{ return false;}
    }

    //Bisher ist mir hier nicht klar, was du damit vor hast? @Max
    /*private synchronized int getClientVersion(){
        return clientVersion;
    }
    private synchronized void setClientVersion(int _clientVersion){
        clientVersion = _clientVersion;
    }*/
}
