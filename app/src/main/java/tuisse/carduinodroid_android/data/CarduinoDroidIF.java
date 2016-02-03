package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 02.02.2016.
 */
public interface CarduinoDroidIF extends CarduinoIF {

    LayerDrawable getIpConnLogoId();
    String getRemoteIp();
    String getTransceiverIp();
    ConnectionState getIpState();
    IpType getIpType();

    void setIpState(ConnectionState is);
    void setIpType(IpType it);

    int getCameraType();
    int getCameraResolution();
    int getCameraFlashlight();
    int getCameraQuality();

    int getSoundPlay();
    int getSoundRecord();

    void setCameraType(int _cameraType);
    void setCameraResolution(int _cameraResolution);
    void setCameraFlashlight(int _cameraFlashlight);
    void setCameraQuality(int _cameraQuality);

    void setSoundPlay(int _soundPlay);
    void setSoundRecord(int _soundRecord);

    int getGpsData();
    int getVibration();
    int getMobileAvailable();
    int getMobileActive();
    int getWLANAvailable();
    int getWLANActive();
    void setGpsData(int _gpsData);
    void setVibration(int _vibration);
    void setMobileAvailable(int _mobileAvailable);
    void setMobileActive(int _mobileActive);
    void setWlanAvailable(int _wlanAvailable);
    void setWlanActive(int _wlanActive);
}
