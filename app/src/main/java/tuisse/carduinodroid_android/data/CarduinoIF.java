package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 01.02.2016.
 */
public interface CarduinoIF {
    int START_BYTE = 0x80;

    float CURRENT_DEVIDER = 10.0f;
    float ABS_BATT_DEVIDER = 1.0f;
    float PER_BATT_DEVIDER = 1.0f;
    float VOLTAGE_DEVIDER = 10.0f;
    float TEMPERATURE_DEVIDER = 2.0f;
    float ULTRASOUND_FRONT_DEVIDER = 1.0f;
    float ULTRASOUND_BACK_DEVIDER = 1.0f;

    int SPEED_MAX = 127;
    int STEER_MAX = 127;

    void setSerialState(ConnectionState state);
    void setSerialName(String s);
    void setSerialType(SerialType type);

    ConnectionState getSerialState();
    String getSerialName();
    SerialType getSerialType();
    LayerDrawable getSerialConnLogoId(SerialType serialPref);

    float getCurrentFloat();
    float getAbsBattCapFloat();
    float getRelBattCapFloat();
    float getVoltageFloat();
    float getTemperatureFloat();
    float getUltrasoundFrontFloat();
    float getUltrasoundBackFloat();

    void setCurrent(int _current);
    void setAbsBattCap(int _absBattCap);
    void setRelBattCap(int _relBattCap);
    void setVoltage(int _voltage);
    void setTemperature(int _temperature);
    void setUltrasoundFront(int _ultrasoundFront);
    void setUltrasoundBack(int _ultrasoundBack);

    int getCurrent();
    int getAbsBattCap();
    int getRelBattCap();
    int getVoltage();
    int getTemperature();
    int getUltrasoundFront();
    int getUltrasoundBack();

    byte getSpeed();
    byte getSteer();
    int  getStatusLed();
    int  getFrontLight();
    int  getResetAccCur();
    int  getFailSafeStop();

    void setSpeed(int _speed);
    void setSteer(int _steer);
    void setStatusLed(int _statusLed);
    void setFrontLight(int _frontLight);
    void setResetAccCur(int _resetAccCur);
    void setFailSafeStop(int _failSafeStop);

    void resetMotors();
}
