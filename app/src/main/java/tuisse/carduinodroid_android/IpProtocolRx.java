package tuisse.carduinodroid_android;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bird on 28.01.2016.
 */
public class IpProtocolRx {

    private final String TAG = "CarduinoIpRx";
    private final int VERSION = 2;

    private int version;
    private int dataType;

    private int carSpeed; // PWM-modulated signal: 0-FF (Hex) -> translated into Dez: 0-255
    private int carSpeedDirection; // Forward=1 - Backward=0
    private int carSteerAngle; // PWM-modulated signal: 0-FF (Hex) -> translated into Dez: 0-255
    private int carSteerDirection; // Left= - Right=

    private int cameraType; // Front= - Back=
    private int cameraResolution; // Defines position of the resolution out of an String-Array with
                                  // all supported ones
    private int cameraFlashlight; // On=1 - Off=0;
    private int cameraQuality; // Value between 0(low) to 100 (high)

    private int soundPlay; // Play a horn sound=1 - No horn sound=0;
    private int soundRecord; // Start Recording=1 - Stop Recording=0;


    public IpProtocolRx() {

        reset();
    }

    public synchronized void reset(){

        version = 0;
        dataType = 0;

        carSpeed = 0;
        carSpeedDirection = 0;
        carSteerAngle = 0;
        carSteerDirection = 0;

        cameraType = 0;
        cameraResolution = 0;
        cameraFlashlight = 0;
        cameraQuality = 100;

        soundPlay = 0;
        soundRecord = 0;
    }

    public synchronized int getVersion(){ return version;}
    public synchronized int getDataType(){ return dataType;}

    public synchronized int getCarSpeed(){ return carSpeed;}
    public synchronized int getCarSpeedDirection(){ return carSpeedDirection;}
    public synchronized int getCarSteerAngle(){ return carSteerAngle;}
    public synchronized int getCarSteerDirection(){ return carSteerDirection;}

    public synchronized int getCameraType(){ return cameraType;}
    public synchronized int getCameraResolution(){ return cameraResolution;}
    public synchronized int getCameraFlashlight(){ return cameraFlashlight;}
    public synchronized int getCameraQuality(){ return cameraQuality;}

    public synchronized int getSoundPlay(){ return soundPlay;}
    public synchronized int getSoundRecord(){ return soundRecord;}

    // translate a received JSON object to get all the send information out of it and set it to
    // their variables
    public synchronized boolean parseJson(JSONObject JsonObjectRxData) {

        try {
            JSONObject JsonObjectHeader = JsonObjectRxData.getJSONObject("Header");

            version = JsonObjectHeader.getInt("Version");
            if(this.version != VERSION) {
                Log.e(TAG, "Wrong JSON version between Transmitter and Receiver");
                return false;
            }


        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
