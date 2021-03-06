package tuisse.carduinodroid_android.data;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import tuisse.carduinodroid_android.Constants;

/**
 * <h1>IP Frame Handler</h1>
 * This Class is used for the preparation of all the transmission data between remote and
 * transceiver. One part contains the Creation of a certain transmission protocol based on JSON.
 * Both sides need to use the same type of JSON and to check this factor a Header is introduced in
 * all of the packages. The Header contains of version, data mask and the status of the data socket.
 *
 * @author Lars Vogel
 * @author Till Max Schwikal
 * @version 1.0
 * @since 03.02.2016
 */
public class IpFrameHandler implements IpFrameIF{
    private final String TAG = "CarduinoIpFrame";
    private CarduinoDroidData carduinoDroidData;
    private CarduinoData carduinoData;

    private JSONObject JsonObjectData;
    private boolean isMaskTypeServer;

    private boolean isCar;
    private boolean isMobility;
    private boolean isFeature;
    private boolean isHardware;
    private boolean isVideo;
    private boolean isControl;
    private boolean isCamera;
    private boolean isSound;
    private boolean isSerial;

    private boolean isForClient;
    private boolean isForServer;

    /**
     * The constructor is important to get the access to the data base provided by CarduinoData and
     * CarduinoDroidData.
     * @param cd gives the access to the CarduinoData
     * @param cdd gives the access to the CarduinoDroidData
     */
    public IpFrameHandler(CarduinoData cd, CarduinoDroidData cdd){
        carduinoDroidData = cdd;
        carduinoData = cd;
        //setClientVersion(0);
    }

    /**
     * This method is the essential part for transmitting data between transceiver and remote side.
     * It enables the translation of received JSON objects to get all the send information out of it
     * and set it to their variables.
     * @param jsonObjectRxData is the receive message as String and it will be transformed to a JSON
     *                         object
     * @return either the information if the data socket is already in use or what data mask is used
     * in a certain package to set the Intent in the IP Connection
     * @see tuisse.carduinodroid_android.IpConnection
     */
    public synchronized String parseJson(String jsonObjectRxData) {
        // return isConnectedRunning = true if Data Server is already connected/running or if there
        // are some errors occurring
        try {
            JSONObject jsonObject = new JSONObject(jsonObjectRxData) ;

            JSONObject JsonObjectHeader = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_HEADER);
            //First Check if both sides (Remote & Transceiver) using the same JSON Version
            if(JsonObjectHeader.getInt(Constants.JSON_OBJECT.TAG_HEADER_VERSION) != Constants.JSON_OBJECT.MY_VERSION) {
                Log.e(TAG, "Wrong JSON Version between Transmitter and Receiver");
                return "true";
            }else{
                String mask = JsonObjectHeader.getString(Constants.JSON_OBJECT.TAG_HEADER_INFORMATION_TYPE);
                if(isMaskLogic(mask)){
                    //Define the Type for the right Parsing
                    //Log.i(TAG, String.valueOf(JsonObjectHeader.getBoolean(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS)));
                    if(!isForServer&&!isForClient){
                        //Ctrl Server Socket Data: Information about Data Server Socket Status as feedback
                        //test if a boolean value will be shown as String
                        return JsonObjectHeader.getString(Constants.JSON_OBJECT.TAG_HEADER_DATA_SERVER_STATUS);
                    }else if(isForClient){
                        //Parsing all the information given from a Server to the Client
                        if(isCar){
                            JSONObject JsonObjectCarInfo = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CAR);

                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_CURRENT))
                                carduinoData.setCurrent(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_CURRENT));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_BATTERY_ABSOLUTE))
                                carduinoData.setAbsBattCap(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_BATTERY_ABSOLUTE));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_BATTERY_PERCENTAGE))
                                carduinoData.setRelBattCap(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_BATTERY_PERCENTAGE));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_VOLTAGE))
                                carduinoData.setVoltage(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_VOLTAGE));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_TEMPERATURE))
                                carduinoData.setTemperature(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_TEMPERATURE));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_FRONT))
                                carduinoData.setUltrasoundFront(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_FRONT));
                            if(!JsonObjectCarInfo.isNull(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_BACK))
                                carduinoData.setUltrasoundBack(JsonObjectCarInfo.getInt(Constants.JSON_OBJECT.TAG_CAR_ULTRASONIC_BACK));
                        }
                        if(isMobility){
                            JSONObject JsonObjectMobility = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_MOBILITY);

                            if(!JsonObjectMobility.isNull(Constants.JSON_OBJECT.TAG_MOBILITY_GPS))
                                carduinoDroidData.setGpsData(JsonObjectMobility.getString(Constants.JSON_OBJECT.TAG_MOBILITY_GPS));
                            if(!JsonObjectMobility.isNull(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_AVAILABLE))
                                carduinoDroidData.setMobileAvailable(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_AVAILABLE));
                            if(!JsonObjectMobility.isNull(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_ACTIVE))
                                carduinoDroidData.setMobileActive(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_ACTIVE));
                            if(!JsonObjectMobility.isNull(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_AVAILABLE))
                                carduinoDroidData.setWlanAvailable(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_AVAILABLE));
                            if(!JsonObjectMobility.isNull(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_ACTIVE))
                                carduinoDroidData.setWlanActive(JsonObjectMobility.getInt(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_ACTIVE));
                        }
                        if(isFeature){
                            JSONObject JsonObjectFeature = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_FEATURES);

                            if(!JsonObjectFeature.isNull(Constants.JSON_OBJECT.TAG_FEATURES_VIBRATION))
                                carduinoDroidData.setVibration((float) JsonObjectFeature.getDouble(Constants.JSON_OBJECT.TAG_FEATURES_VIBRATION));
                            if(!JsonObjectFeature.isNull(Constants.JSON_OBJECT.TAG_FEATURES_BATTERY_PHONE))
                                carduinoDroidData.setBatteryPhone((float) JsonObjectFeature.getDouble(Constants.JSON_OBJECT.TAG_FEATURES_BATTERY_PHONE));
                        }
                        if(isHardware){
                            JSONObject JsonObjectHardware = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_HARDWARE);

                            if(!JsonObjectHardware.isNull(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM) && !JsonObjectHardware.isNull(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION))
                            carduinoDroidData.setCameraSupportedSizes(getSupportedSizedValues(JsonObjectHardware.getJSONObject(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION),
                                    JsonObjectHardware.getInt(Constants.JSON_OBJECT.TAG_HARDWARE_CAMERA_RESOLUTION_NUM)));
                        }
                        if(isVideo){
                            JSONObject JsonObjectVideo = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_VIDEO);

                            if(!JsonObjectVideo.isNull(Constants.JSON_OBJECT.TAG_VIDEO_SOURCE)){
                                String PictureFrameBase64Encoded = JsonObjectVideo.getString(Constants.JSON_OBJECT.TAG_VIDEO_SOURCE);
                                byte[] PictureFrameBase64Decoded = Base64.decode(PictureFrameBase64Encoded, Base64.DEFAULT);
                                carduinoDroidData.setCameraPicture(PictureFrameBase64Decoded);}
                        }
                        if(isSerial){
                            JSONObject JsonObjectSerial = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_SERIAL);

                            if(!JsonObjectSerial.isNull(Constants.JSON_OBJECT.TAG_SERIAL_STATUS) && !JsonObjectSerial.isNull(Constants.JSON_OBJECT.TAG_SERIAL_ERROR))
                                carduinoData.setSerialState(new ConnectionState(ConnectionEnum.fromInteger(JsonObjectSerial.getInt(Constants.JSON_OBJECT.TAG_SERIAL_STATUS)),
                                    JsonObjectSerial.getString(Constants.JSON_OBJECT.TAG_SERIAL_ERROR)));
                            if(!JsonObjectSerial.isNull(Constants.JSON_OBJECT.TAG_SERIAL_NAME))
                                carduinoData.setSerialName(JsonObjectSerial.getString(Constants.JSON_OBJECT.TAG_SERIAL_NAME));
                            if(!JsonObjectSerial.isNull(Constants.JSON_OBJECT.TAG_SERIAL_TYPE))
                                carduinoData.setSerialType(SerialType.fromInteger(JsonObjectSerial.getInt(Constants.JSON_OBJECT.TAG_SERIAL_TYPE)));
                        }
                        return mask;
                    }else{
                        //Parsing all the information given from a Client to the Server
                        if(isControl){
                            JSONObject JsonObjectControl = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CONTROL);

                            if(!JsonObjectControl.isNull(Constants.JSON_OBJECT.TAG_CONTROL_SPEED))
                                carduinoData.setSpeed(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_SPEED));
                            if(!JsonObjectControl.isNull(Constants.JSON_OBJECT.TAG_CONTROL_STEER))
                                carduinoData.setSteer(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_STEER));
                            if(!JsonObjectControl.isNull(Constants.JSON_OBJECT.TAG_CONTROL_FRONT_LIGHT))
                                carduinoData.setFrontLight(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_FRONT_LIGHT));
                            if(!JsonObjectControl.isNull(Constants.JSON_OBJECT.TAG_CONTROL_STATUS_LED))
                                carduinoData.setStatusLed(JsonObjectControl.getInt(Constants.JSON_OBJECT.TAG_CONTROL_STATUS_LED));
                        }
                        if(isCamera){
                            JSONObject JsonObjectCamera = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_CAMERA);

                            if(!JsonObjectCamera.isNull(Constants.JSON_OBJECT.TAG_CAMERA_TYPE))
                                carduinoDroidData.setCameraType(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_TYPE));
                            if(!JsonObjectCamera.isNull(Constants.JSON_OBJECT.TAG_CAMERA_RESOLUTION))
                                carduinoDroidData.setCameraResolutionID(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_RESOLUTION));
                            if(!JsonObjectCamera.isNull(Constants.JSON_OBJECT.TAG_CAMERA_LIGHT))
                                carduinoDroidData.setCameraFlashlight(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_LIGHT));
                            if(!JsonObjectCamera.isNull(Constants.JSON_OBJECT.TAG_CAMERA_QUALITY))
                                carduinoDroidData.setCameraQuality(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_QUALITY));
                            //carduinoDroidData.setCameraDegree(JsonObjectCamera.getInt(Constants.JSON_OBJECT.TAG_CAMERA_ORIENTATION));
                        }
                        if(isSound){
                            JSONObject JsonObjectSound = jsonObject.getJSONObject(Constants.JSON_OBJECT.TAG_SOUND);

                            if(!JsonObjectSound.isNull(Constants.JSON_OBJECT.TAG_SOUND_PLAY))
                                carduinoDroidData.setSoundPlay(JsonObjectSound.getInt(Constants.JSON_OBJECT.TAG_SOUND_PLAY));
                            if(!JsonObjectSound.isNull(Constants.JSON_OBJECT.TAG_SOUND_RECORD))
                                carduinoDroidData.setSoundRecord(JsonObjectSound.getInt(Constants.JSON_OBJECT.TAG_SOUND_RECORD));
                        }
                        return mask;
                    }
                }else{
                    Log.e(TAG, "Error on JSON Parsing by mixed up Data inclusion (Both Client and Server Data)");
                    return "true";
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error on JSON Parsing by missing information)");
            e.printStackTrace();
            return "true";
        }
    }

    /**
     * This methods checks a given data mask if it contains predefined keywords and if the given
     * combination is Logic. Only Client or only Server data should be put together. And it saves
     * the extracted information for the following parsing process.
     * @param mask is the given data mask in a certain JSON Object
     * @return if the data mask is logic
     */
    private synchronized boolean isMaskLogic(String mask){
        isCar = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAR);
        isMobility = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_MOBILITY);
        isFeature = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_FEATURES);
        isHardware = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_HARDWARE);
        isVideo = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_VIDEO);
        isControl = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CONTROL);
        isCamera = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_CAMERA);
        isSound = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_SOUND);
        isSerial = checkDataTypeMask(mask,Constants.JSON_OBJECT.NUM_SERIAL);

        isForClient = (isCar || isMobility || isFeature || isHardware || isVideo || isSerial);
        isForServer = (isControl || isCamera ||isSound);

        return !(isForClient&&isForServer);
    }

    /**
     * This method is an essential part for the data exchange between the transceiver and a remote
     * device. A Standard Build is defined by a Header and if it is not a Control Message, it
     * contains some data objects as well depending on the data type mask.
     * @param dataTypeMask shows this method what parts should be integrated (camera, car, control
     *                     or other data)
     * @param dataServerStatus is important if this method is only used for control messages
     * @return the created JSON object which is send a string over the established socket
     */
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
            // Collect all the mobility information (GPS and available Networks) for the JSON Object to the
            // remote side
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_MOBILITY)) {

                JSONObject JsonObjectMobilityInformation = new JSONObject();

                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_GPS, carduinoDroidData.getGpsData());
                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_AVAILABLE, carduinoDroidData.getMobileAvailable());
                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_MOBILE_ACTIVE, carduinoDroidData.getMobileAvailable());
                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_AVAILABLE, carduinoDroidData.getWLANAvailable());
                JsonObjectMobilityInformation.put(Constants.JSON_OBJECT.TAG_MOBILITY_WLAN_ACTIVE, carduinoDroidData.getWLANActive());

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
            /**** Including extra Features ****/
            // rarely updated information based on changed values like
            // Collect all the network information for the JSON Object to the remote side
            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_FEATURES)) {

                JSONObject JsonObjectFeatures = new JSONObject();

                JsonObjectFeatures.put(Constants.JSON_OBJECT.TAG_FEATURES_VIBRATION, carduinoDroidData.getVibration());
                JsonObjectFeatures.put(Constants.JSON_OBJECT.TAG_FEATURES_BATTERY_PHONE, carduinoDroidData.getBatteryPhone());

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_FEATURES, JsonObjectFeatures);
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

            if (checkDataTypeMask(dataTypeMask,Constants.JSON_OBJECT.NUM_SERIAL)) {

                JSONObject JsonObjectSerialInformation = new JSONObject();

                JsonObjectSerialInformation.put(Constants.JSON_OBJECT.TAG_SERIAL_STATUS, ConnectionEnum.toInteger(carduinoData.getSerialState().getState()));
                JsonObjectSerialInformation.put(Constants.JSON_OBJECT.TAG_SERIAL_ERROR, carduinoData.getSerialState().getError());
                JsonObjectSerialInformation.put(Constants.JSON_OBJECT.TAG_SERIAL_NAME, carduinoData.getSerialName());
                JsonObjectSerialInformation.put(Constants.JSON_OBJECT.TAG_SERIAL_TYPE, SerialType.toInteger(carduinoData.getSerialType()));

                JsonObjectData.put(Constants.JSON_OBJECT.TAG_SERIAL, JsonObjectSerialInformation);
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
                    //JsonObjectCameraInformation.put(Constants.JSON_OBJECT.TAG_CAMERA_ORIENTATION, carduinoDroidData.getCameraDegree());

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

    /**
     * This method is the public method to get the created JSON object if it was successful.
     * Otherwise this methods will give a null object to check this possible error.
     * @param dataTypeMask shows this method what parts should be integrated (camera, car, control
     *                     or other data)
     * @param dataServerStatus is important if this method is only used for control messages
     * @return the created JSON or null object
     */
    public synchronized JSONObject getTransmitData(String dataTypeMask, boolean dataServerStatus){

        if(createJsonObject(dataTypeMask, dataServerStatus))
            return JsonObjectData;
        else return null;
    }

    /**
     * This method is the central part to check a given data type mask string if it contains
     * a key word.
     * @param dataTypeMask contains the full string given in the JSON Object
     * @param Type is the key word this methods wants to check for
     * @return if the key word is in the mask (true) or not (false)
     */
    private synchronized boolean checkDataTypeMask(String dataTypeMask, String Type){

        if(dataTypeMask.contains(Type)){
            return true;
        }else{ return false;}
    }

    /**
     * This method transforms the Supported Sizes Array from the camera to a format that can be
     * send with a JSON Object.
     * @return a JSON object containing all the possible sizes
     */
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

    /**
     * This method is the opposite direction as setSupportedSizesObject() to create an Array out of
     * the given JSON Object to use on the DriveActivity
     * @param JsonObjectCameraSizes is the JSON Object containing the supported sizes and which
     *                              shall be transformed here
     * @param count indicates the number of values in the JSON Object
     * @return the array of supported camera sizes
     */
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
