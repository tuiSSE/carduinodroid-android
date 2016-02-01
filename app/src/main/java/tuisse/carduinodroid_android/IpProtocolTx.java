package tuisse.carduinodroid_android;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bird on 28.01.2016.
 */
public class IpProtocolTx {

    private final String TAG = "CarduinoIpTx";
    private final int VERSION = 2;

    private static final String TAG_HEADER_VERSION = "Version";
    private static final String TAG_HEADER_INFORMATION_TYPE = "Information Type";
    private static final String TAG_HEADER = "Header";

    private static final String TAG_CAR_CURRENT = "Current";
    private static final String TAG_CAR_BATTERY_ABSOLUTE = "Battery Absolute";
    private static final String TAG_CAR_BATTERY_PERCENTAGE = "Battery Percentage";
    private static final String TAG_CAR_VOLTAGE = "Voltage";
    private static final String TAG_CAR_TEMPERATURE = "Temperature";
    private static final String TAG_CAR_ULTRASONIC_FRONT = "Ultra Sonic Front";
    private static final String TAG_CAR_ULTRASONIC_BACK = "Ultra Sonic Back";
    private static final String TAG_CAR = "Car Information";
    private final int NUM_CAR = 0b0001;

    private static final String TAG_MOBILITY_GPS = "GPS Data";
    private static final String TAG_MOBILITY_VIBRATION = "Vibration Value";
    private static final String TAG_MOBILITY = "Mobilty Information";
    private final int NUM_MOBILITY = 0b0010;

    private static final String TAG_NETWORK_WLAN_AVAILABLE = "WLAN Available";
    private static final String TAG_NETWORK_WLAN_ACTIVE = "WLAN Active";
    private static final String TAG_NETWORK_MOBILE_AVAILABLE = "Mobile Available";
    private static final String TAG_NETWORK_MOBILE_ACTIVE = "Mobile Active";
    private static final String TAG_NETWORK = "Network Information";
    private final int NUM_NETWORK = 0b0100;

    private static final String TAG_HARDWARE_CAMERA_RESOLUTION = "Camera Resolution";
    private static final String TAG_HARDWARE = "Hardware Information";
    private final int NUM_HARDWARE = 0b1000;

    //
    private int carCurrent;//in 0.1mA
    private int absoluteBattery;//in 1mAh
    private int percentBattery;//in %
    private int voltage;//in 0.1V
    private int carTemperature;//in 0.5°C
    private int ultraSonicFront;//in 1cm
    private int ultraSonicBack;//in 1cm

    private int gpsData;//
    private int vibrationValue;//

    private int mobileAvail;//
    private int mobileActive;//
    private int wlanAvail;//
    private int wlanActive;//

    private int cameraResolution;//

    private JSONObject JsonObjectTxData;

    public IpProtocolTx() {

        reset();
        //Testing Parameter for functions
        //createJsonObject(0b1111,"Test");
    }

    public synchronized void reset(){

        carCurrent = 0;
        absoluteBattery = 0;
        percentBattery = 0;
        voltage = 0;
        carTemperature = 0;
        ultraSonicFront = 0;
        ultraSonicBack = 0;

        gpsData = 0;
        vibrationValue = 0;

        mobileAvail = 0;
        mobileActive = 0;
        wlanAvail = 0;
        wlanActive = 0;

        cameraResolution = 0;
    }

    // Read in all the transmit data to set up the variables
    private synchronized boolean setTransmitInformation(String transmitData) {
        // just talk with Max to set up right
        return true;
    }

    // Standard Build of the transmitted JSON Object defined by Header, Hardware information,
    // Car information, vibration and camera resolution out of the available variables
    public synchronized boolean createJsonObject(int dataTypeMask, String transmitData) {

        if(setTransmitInformation(transmitData))
        {
            try {
                JsonObjectTxData = new JSONObject();
                // Header is used for all the JSON Objects to define Version and Type
                JSONObject JsonObjectHeader = new JSONObject();

                // Create the Header to define version, data type to secure the transmission
                JsonObjectHeader.put(TAG_HEADER_VERSION, getVersion());
                JsonObjectHeader.put(TAG_HEADER_INFORMATION_TYPE, dataTypeMask);

                JsonObjectTxData.put(TAG_HEADER, JsonObjectHeader);

                // often updated information base
                // Collect all the Car information for the JSON Object to the remote side
                if (NUM_CAR == (dataTypeMask & NUM_CAR)) {

                    JSONObject JsonObjectCarInformation = new JSONObject();

                    JsonObjectCarInformation.put(TAG_CAR_CURRENT, getCarCurrent());
                    JsonObjectCarInformation.put(TAG_CAR_BATTERY_ABSOLUTE, getBatteryAbsolute());
                    JsonObjectCarInformation.put(TAG_CAR_BATTERY_PERCENTAGE, getBatteryPercentage());
                    JsonObjectCarInformation.put(TAG_CAR_VOLTAGE, getCarVoltage());
                    JsonObjectCarInformation.put(TAG_CAR_TEMPERATURE, getCarTemperature());
                    JsonObjectCarInformation.put(TAG_CAR_ULTRASONIC_FRONT, getUltraSonicFront());
                    JsonObjectCarInformation.put(TAG_CAR_ULTRASONIC_BACK, getUltraSonicBack());

                    JsonObjectTxData.put(TAG_CAR, JsonObjectCarInformation);
                }
                // Collect all the mobility information (GPS,Vibration) for the JSON Object to the
                // remote side
                if (NUM_MOBILITY == (dataTypeMask & NUM_MOBILITY)) {

                    JSONObject JsonObjectMobilityInformation = new JSONObject();

                    JsonObjectMobilityInformation.put(TAG_MOBILITY_GPS, getGpsData());
                    JsonObjectMobilityInformation.put(TAG_MOBILITY_VIBRATION, getVibrationValue());

                    JsonObjectTxData.put(TAG_MOBILITY, JsonObjectMobilityInformation);
                }

                // rarely updated information based on changed values like
                // Collect all the network information for the JSON Object to the remote side
                if (NUM_NETWORK == (dataTypeMask & NUM_NETWORK)) {

                    JSONObject JsonObjectNetworkInformation = new JSONObject();

                    JsonObjectNetworkInformation.put(TAG_NETWORK_MOBILE_AVAILABLE, getMobileAvailable());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_MOBILE_ACTIVE, getMobileActive());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_WLAN_AVAILABLE, getWLANAvailable());
                    JsonObjectNetworkInformation.put(TAG_NETWORK_WLAN_ACTIVE, getWLANActive());

                    JsonObjectTxData.put(TAG_NETWORK, JsonObjectNetworkInformation);
                }

                // once collected information based on a certain mobile phone setup
                // Collect all the hardware feature based on the mobile phone for the JSON Object
                if (NUM_HARDWARE == (dataTypeMask & NUM_HARDWARE)) {

                    JSONObject JsonObjectHardwareInformation = new JSONObject();

                    JsonObjectHardwareInformation.put(TAG_HARDWARE_CAMERA_RESOLUTION, getCameraResolution());

                    JsonObjectTxData.put(TAG_HARDWARE, JsonObjectHardwareInformation);
                }

                //Log.d(TAG, String.valueOf(BYTE_MASK & NUM_CAR));
                Log.d(TAG, getTransmitData().toString());

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

    private synchronized int getVersion(){ return VERSION;}

    private synchronized int getCarCurrent(){ return carCurrent;}
    private synchronized int getBatteryAbsolute(){ return absoluteBattery;}
    private synchronized int getBatteryPercentage(){ return percentBattery;}
    private synchronized int getCarVoltage(){ return voltage;}
    private synchronized int getCarTemperature(){ return carTemperature;}
    private synchronized int getUltraSonicFront(){ return ultraSonicFront;}
    private synchronized int getUltraSonicBack(){ return ultraSonicBack;}

    private synchronized int getGpsData(){ return gpsData;}
    private synchronized int getVibrationValue(){ return vibrationValue;}

    private synchronized int getMobileAvailable(){ return mobileAvail;}
    private synchronized int getMobileActive(){ return mobileActive;}
    private synchronized int getWLANAvailable(){ return wlanAvail;}
    private synchronized int getWLANActive(){ return wlanActive;}

    private synchronized int getCameraResolution(){ return cameraResolution;}


}
