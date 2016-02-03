package tuisse.carduinodroid_android.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mate on 02.02.2016.
 */
public class IpFrameHandler implements IpFrameIF{
    private final String TAG = "CarduinoIpFrame";
    private CarduinoDroidData carduinoDroidData;

    private final int MY_VERSION = 2;

    private static final String TAG_HEADER_VERSION = "Version";
    private static final String TAG_HEADER_INFORMATION_TYPE = "Information Type";
    private static final String TAG_HEADER = "Header";

    private static final String TAG_CAR_CURRENT = "Current";
    private static final String TAG_CAR_BATTERY_ABSOLUTE = "Battery Absolute";
    private static final String TAG_CAR_BATTERY_PERCENTAGE = "Battery Relative";
    private static final String TAG_CAR_VOLTAGE = "Voltage";
    private static final String TAG_CAR_TEMPERATURE = "Temperature";
    private static final String TAG_CAR_ULTRASONIC_FRONT = "Ultra Sonic Front";
    private static final String TAG_CAR_ULTRASONIC_BACK = "Ultra Sonic Back";
    private static final String TAG_CAR = "Car Information";
    private final String NUM_CAR = "Car";

    private static final String TAG_MOBILITY_GPS = "GPS Data";
    private static final String TAG_MOBILITY_VIBRATION = "Vibration Value";
    private static final String TAG_MOBILITY = "Mobilty Information";
    private final String NUM_MOBILITY = "Mobility";

    private static final String TAG_NETWORK_WLAN_AVAILABLE = "WLAN Available";
    private static final String TAG_NETWORK_WLAN_ACTIVE = "WLAN Active";
    private static final String TAG_NETWORK_MOBILE_AVAILABLE = "Mobile Available";
    private static final String TAG_NETWORK_MOBILE_ACTIVE = "Mobile Active";
    private static final String TAG_NETWORK = "Network Information";
    private final String NUM_NETWORK = "Network";

    private static final String TAG_HARDWARE_CAMERA_RESOLUTION = "Camera Resolution";
    private static final String TAG_HARDWARE = "Hardware Information";
    private final String NUM_HARDWARE = "Hardware";


    private JSONObject JsonObjectTxData;
    private int clientVersion;

    public IpFrameHandler(CarduinoDroidData cdd){
        carduinoDroidData = cdd;
        setClientVersion(0);
    }



    // translate a received JSON object to get all the send information out of it and set it to
    // their variables
    public synchronized boolean parseJson(JSONObject jsonObjectRxData) {
        try {
            JSONObject JsonObjectHeader = jsonObjectRxData.getJSONObject("Header");

            setClientVersion(JsonObjectHeader.getInt("Version"));
            if(getClientVersion() != MY_VERSION) {
                Log.e(TAG, "Wrong JSON version between Transmitter and Receiver");
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    // Read in all the transmit data to set up the variables
    private synchronized boolean setTransmitInformation(String transmitData) {
        // just talk with Max to set up right
        return true;
    }

    // Standard Build of the transmitted JSON Object defined by Header, Hardware information,
    // Car information, vibration and camera resolution out of the available variables
    public synchronized boolean createJsonObject(String dataTypeMask, String transmitData) {

        if(setTransmitInformation(transmitData))
        {
            try {
                JsonObjectTxData = new JSONObject();
                // Header is used for all the JSON Objects to define Version and Type
                JSONObject JsonObjectHeader = new JSONObject();

                // Create the Header to define version, data type to secure the transmission
                JsonObjectHeader.put(TAG_HEADER_VERSION, getClientVersion());
                JsonObjectHeader.put(TAG_HEADER_INFORMATION_TYPE, dataTypeMask);

                JsonObjectTxData.put(TAG_HEADER, JsonObjectHeader);

                // often updated information base
                // Collect all the Car information for the JSON Object to the remote side
                if (checkDataTypeMask(dataTypeMask,NUM_CAR)) {

                    JSONObject JsonObjectCarInformation = new JSONObject();

                    JsonObjectCarInformation.put(TAG_CAR_CURRENT, carduinoDroidData.getCurrent());
                    JsonObjectCarInformation.put(TAG_CAR_BATTERY_ABSOLUTE, carduinoDroidData.getAbsBattCap());
                    JsonObjectCarInformation.put(TAG_CAR_BATTERY_PERCENTAGE, carduinoDroidData.getRelBattCap());
                    JsonObjectCarInformation.put(TAG_CAR_VOLTAGE, carduinoDroidData.getVoltage());
                    JsonObjectCarInformation.put(TAG_CAR_TEMPERATURE, carduinoDroidData.getTemperature());
                    JsonObjectCarInformation.put(TAG_CAR_ULTRASONIC_FRONT, carduinoDroidData.getUltrasoundFront());
                    JsonObjectCarInformation.put(TAG_CAR_ULTRASONIC_BACK, carduinoDroidData.getUltrasoundBack());

                    JsonObjectTxData.put(TAG_CAR, JsonObjectCarInformation);
                }
                // Collect all the mobility information (GPS,Vibration) for the JSON Object to the
                // remote side
                if (checkDataTypeMask(dataTypeMask,NUM_MOBILITY)) {

                    JSONObject JsonObjectMobilityInformation = new JSONObject();

                    JsonObjectMobilityInformation.put(TAG_MOBILITY_GPS, carduinoDroidData.getGpsData());
                    JsonObjectMobilityInformation.put(TAG_MOBILITY_VIBRATION, carduinoDroidData.getVibration());

                    JsonObjectTxData.put(TAG_MOBILITY, JsonObjectMobilityInformation);
                }

                // rarely updated information based on changed values like
                // Collect all the network information for the JSON Object to the remote side
                if (checkDataTypeMask(dataTypeMask,NUM_NETWORK)) {

                    JSONObject JsonObjectNetworkInformation = new JSONObject();

                    JsonObjectNetworkInformation.put(TAG_NETWORK_MOBILE_AVAILABLE, carduinoDroidData.getMobileAvailable());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_MOBILE_ACTIVE, carduinoDroidData.getMobileActive());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_WLAN_AVAILABLE, carduinoDroidData.getWLANAvailable());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_WLAN_ACTIVE, carduinoDroidData.getWLANActive());

                    JsonObjectTxData.put(TAG_NETWORK, JsonObjectNetworkInformation);
                }

                // once collected information based on a certain mobile phone setup
                // Collect all the hardware feature based on the mobile phone for the JSON Object
                if (checkDataTypeMask(dataTypeMask,NUM_HARDWARE)) {

                    JSONObject JsonObjectHardwareInformation = new JSONObject();

                    JsonObjectHardwareInformation.put(TAG_HARDWARE_CAMERA_RESOLUTION, carduinoDroidData.getCameraResolution());

                    JsonObjectTxData.put(TAG_HARDWARE, JsonObjectHardwareInformation);
                }

                //Log.d(TAG, String.valueOf(BYTE_MASK & NUM_CAR));
                //Log.d(TAG, getTransmitData().toString());

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        else {return false;}

        return true;
    }

    // Return the last created JSON Object with the current variables
    public synchronized JSONObject getTransmitData(){

        return JsonObjectTxData;
    }

    private synchronized boolean checkDataTypeMask(String dataTypeMask, String Type){

        if(dataTypeMask.contains(Type)){
            return true;
        }else{ return false;}
    }

    private synchronized int getClientVersion(){
        return clientVersion;
    }
    private synchronized void setClientVersion(int _clientVersion){
        clientVersion = _clientVersion;
    }
}
