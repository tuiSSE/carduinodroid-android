package tuisse.carduinodroid_android.data;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
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
    private boolean isMaskTypeServer;

    private boolean isCar;
    private boolean isMobility;
    private boolean isNetwork;
    private boolean isHardware;
    private boolean isVideo;
    private boolean isControl;
    private boolean isCamera;
    private boolean isSound;

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
                    Log.i(TAG, String.valueOf(JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS)));
                    if(!isForServer&&!isForClient){
                        //Ctrl Server Socket Data: Information about Data Server Socket Status as feedback
                        return JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS);
                    }else if(isForClient){
                        //Parsing all the information given from a Server to the Client
                        if(isCar){
                            JSONObject JsonObjectCarInfo = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CAR);

                            carduinoData.setCurrent(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_CURRENT));
                            carduinoData.setAbsBattCap(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_BATTERY_ABSOLUTE));
                            carduinoData.setRelBattCap(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_BATTERY_PERCENTAGE));
                            carduinoData.setVoltage(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_VOLTAGE));
                            carduinoData.setTemperature(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_TEMPERATURE));
                            carduinoData.setUltrasoundFront(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_FRONT));
                            carduinoData.setUltrasoundBack(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_BACK));
                        }
                        if(isMobility){
                            JSONObject JsonObjectMobility = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_MOBILITY);

                            carduinoDroidData.setGpsData(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_GPS));
                            carduinoDroidData.setVibration(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_VIBRATION));
                        }
                        if(isHardware){
                            JSONObject JsonObjectHardware = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_HARDWARE);
                            //TODO give all the Resolutions to show whats possible and the ID of the actual chosen one
                            carduinoDroidData.setCameraSupportedSizes(getSupportedSizedValues(JsonObjectHardware.getJSONObject(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION),
                                    JsonObjectHardware.getInt(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM)));
                        }
                        if(isHardware){
                            //TO-DO ? Maybe we can leave it out in this app because i see no value out of it
                        }
                        if(isVideo){
                            JSONObject JsonObjectVideo = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_VIDEO);

                            String PictureFrameBase64Encoded = JsonObjectVideo.getString(Constants.JSON_OBJECT.TAG_VIDEO_SOURCE);
                            byte[] PictureFrameBase64Decoded = Base64.decode(PictureFrameBase64Encoded, Base64.DEFAULT);
                            carduinoDroidData.setCameraPicture(PictureFrameBase64Decoded);
                        }
                        return JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS);
                    }else{
                        //Parsing all the information given from a Client to the Server
                        if(isControl){
                            JSONObject JsonObjectControl = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CONTROL);

                            carduinoData.setSpeed(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_SPEED));
                            carduinoData.setSteer(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_STEER));
                            carduinoData.setFrontLight(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_FRONT_LIGHT));
                            carduinoData.setStatusLed(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_STATUS_LED));
                        }
                        if(isCamera){
                            JSONObject JsonObjectCamera = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CAMERA);

                            carduinoDroidData.setCameraType(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_TYPE));
                            carduinoDroidData.setCameraResolutionID(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_RESOLUTION));
                            carduinoDroidData.setCameraFlashlight(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_LIGHT));
                            carduinoDroidData.setCameraQuality(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_QUALITY));
                            //carduinoDroidData.setCameraDegree(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_ORIENTATION));
                        }
                        if(isSound){
                            JSONObject JsonObjectSound = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_SOUND);

                            carduinoDroidData.setSoundPlay(JsonObjectSound.getInt(Constants.JSON_OBJECT.TAG_SOUND_PLAY));
                            carduinoDroidData.setSoundRecord(JsonObjectSound.getInt(Constants.JSON_OBJECT.TAG_SOUND_RECORD));
                        }
                        return true;
                    }
                }else{
                    Log.e(TAG, "Error on JSON Parsing by mixed up Data inclusion (Both Client and Server Data)");
                    return true;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error on JSON Parsing by missing information)");
            e.printStackTrace();
            return true;
        }
    }

    private boolean isMaskLogic(String mask){

        isCar = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAR);
        isMobility = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_MOBILITY);
        isNetwork = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_NETWORK);
        isHardware = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_HARDWARE);
        isVideo = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_VIDEO);
        isControl = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CONTROL);
        isCamera = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAMERA);
        isSound = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_SOUND);

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


                byte[] PictureFrame = carduinoDroidData.getCameraPicture();
                if (PictureFrame != null){
                    String PictureFrameBase64Encoded = Base64.encodeToString(PictureFrame, Base64.DEFAULT);

                    JsonObjectVideoData.put(Constants.JSON_OBJECT.TAG_VIDEO_TYPE, "");
                    JsonObjectVideoData.put(Constants.JSON_OBJECT.TAG_VIDEO_SOURCE, PictureFrameBase64Encoded);
                }else{
                    JsonObjectVideoData.put(Constants.JSON_OBJECT.TAG_VIDEO_TYPE, "");
                    JsonObjectVideoData.put(Constants.JSON_OBJECT.TAG_VIDEO_SOURCE, "");
                }
                JsonObjectData.put(Constants.JSON_OBJECT.TAG_VIDEO, JsonObjectVideoData);
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

                if(carduinoDroidData.getCameraSupportedSizes() != null) {
                    JsonObjectHardwareInformation.put(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM, carduinoDroidData.getCameraSupportedSizes().length);
                    JsonObjectHardwareInformation.put(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION, setSupportedSizesObject());
                }
                JsonObjectData.put(Constants.JSON_OBJECT.TAG_HARDWARE, JsonObjectHardwareInformation);
                isMaskTypeServer = true;
            }

            /**** Starting with Client Paket Part ****/
            /**** Including Car Control ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_CONTROL)) {

                if(isMaskTypeServer) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else{
                    JSONObject JsonObjectCarControl = new JSONObject();

                    JsonObjectCarControl.put(Constants.JSON_OBJECT.TAG_CONTROL_SPEED, carduinoData.getSpeed());
                    JsonObjectCarControl.put(Constants.JSON_OBJECT.TAG_CONTROL_STEER, carduinoData.getSteer());
                    JsonObjectCarControl.put(Constants.JSON_OBJECT.TAG_CONTROL_FRONT_LIGHT, carduinoData.getFrontLight());
                    JsonObjectCarControl.put(Constants.JSON_OBJECT.TAG_CONTROL_STATUS_LED, carduinoData.getStatusLed());

                    JsonObjectData.put(Constants.JSON_OBJECT.TAG_CONTROL, JsonObjectCarControl);
                }
            }
            /**** Including Camera Information ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_CAMERA)) {
                if(isMaskTypeServer) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else {
                    JSONObject JsonObjectCameraInformation = new JSONObject();

                    JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_TYPE, carduinoDroidData.getCameraType());
                    JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_RESOLUTION, carduinoDroidData.getCameraResolutionID());
                    JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_LIGHT, carduinoDroidData.getCameraFlashlight());
                    JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_QUALITY, carduinoDroidData.getCameraQuality());
                    JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_ORIENTATION, carduinoDroidData.getCameraDegree());

                    JsonObjectData.put(Constants.JSON_OBJECT.TAG_CAMERA, JsonObjectCameraInformation);
                }
            }
            /**** Including Sound Options ****/
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_SOUND)) {
                if(isMaskTypeServer) {
                    Log.i(TAG,"Error on Creating JSON Object - Mixed up Types of Server and Client");
                    return false;
                }
                else {
                    JSONObject JsonObjectSoundOptions = new JSONObject();
                    // TO-DO
                    JsonObjectSoundOptions.put(Constants.JSON_OBJECT.TAG_SOUND_PLAY, carduinoDroidData.getSoundPlay());
                    JsonObjectSoundOptions.put(Constants.JSON_OBJECT.TAG_SOUND_RECORD, carduinoDroidData.getSoundRecord());

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

    private synchronized JSONObject setSupportedSizesObject(){

        JSONObject JsonObjectCameraSizes = new JSONObject();

        String[] resolutionSizes = carduinoDroidData.getCameraSupportedSizes();
        if(resolutionSizes != null) {

            int numValues = resolutionSizes.length;
            try {
                for (int i = 0; i < numValues; i++) {

                    JsonObjectCameraSizes.put(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM + i,resolutionSizes[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return JsonObjectCameraSizes;
    }

    private synchronized String[] getSupportedSizedValues(JSONObject JsonObjectCameraSizes, int count){

        if(count > 0){
            String[] values = new String[count];
            try {
                for (int i = 0; i < count; i++) {
                    values[i] = JsonObjectCameraSizes.getString(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM+i);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
            //carduinoDroidData.setCameraResolutionID(count-1);
            return values;
        }

        return null;
    }
}
