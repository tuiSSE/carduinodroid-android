package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 02.02.2016.
 */
public interface CarduinoDroidIF{

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
