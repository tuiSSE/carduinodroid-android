package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * <h1>CarduinoDroidData Interface</h1>
 * The CarDuinoDroidData Interface implements a full database for all the values transmitted between
 * transceiver and remote side. It is divived in camera and sound settings, mobile and WLAN
 * information, battery status, vibration for a possible gamepad. Here you can find all the possible
 * functions and methods provides by the CarduinoDroidData Class
 *
 * @author Till Max Schwikal
 * @author Lars Vogel
 * @version 1.0
 * @since 03.02.2016
 */
public interface CarduinoDroidIF{

    /**
     * Integration of all the methods and functions out of the CarduinoDroidData as an Interface
     * so you can use them a certain points in the actual program. It also provides a centralized
     * data management.
     */
    LayerDrawable getIpConnLogoId();
    String getRemoteIp();
    String getTransceiverIp();
    ConnectionState getIpState();
    IpType getIpType();

    void setIpState(ConnectionState is);
    void setIpType(IpType it);

    void resetValues();

    int getCameraType();
    int getCameraResolutionID();
    String[] getCameraSupportedSizes();
    int getCameraFlashlight();
    int getCameraQuality();
    byte[] getCameraPicture();

    int getSoundPlay();
    int getSoundRecord();

    void setCameraType(int _cameraType);
    void setCameraSupportedSizes(String[] _cameraSupportedSizes);
    void setCameraResolutionID(int _cameraResolutionID);
    void setCameraFlashlight(int _cameraFlashlight);
    void setCameraQuality(int _cameraQuality);
    void setCameraPicture(byte[] _cameraPicture);

    void setSoundPlay(int _soundPlay);
    void setSoundRecord(int _soundRecord);

    float getBatteryPhone();
    String getGpsData();
    float getVibration();

    int getMobileAvailable();
    int getMobileActive();
    int getWLANAvailable();
    int getWLANActive();

    void setBatteryPhone (float _battery);
    void setGpsData(String _gpsData);
    void setVibration(float _vibration);

    void setMobileAvailable(int _mobileAvailable);
    void setMobileActive(int _mobileActive);
    void setWlanAvailable(int _wlanAvailable);
    void setWlanActive(int _wlanActive);



}
