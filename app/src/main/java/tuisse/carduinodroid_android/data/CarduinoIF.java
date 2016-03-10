package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * <h1>CarduinoData Interface</h1>
 * This interface holds all constants and functions needed for the CarduinoData class.
 *
 * @author Till Max Schwikal
 * @version 1.0
 * @since 01.02.2016
 *
 * @see tuisse.carduinodroid_android.data.CarduinoData
 */
public interface CarduinoIF {

    int BYTE_MASK                   = 0xff;///< byte-length mask
    int START_BYTE                  = 0x80;///< start byte

    float CURRENT_DEVIDER           = 10.0f;///< current devider for 1A
    float ABS_BATT_DEVIDER          = 1.0f;///< absolute battery capacity devider for 1mAh
    float PER_BATT_DEVIDER          = 1.0f;///< relative battery capacity devider for 1%
    float VOLTAGE_DEVIDER           = 10.0f;///< voltage devider for 1V
    float TEMPERATURE_DEVIDER       = 2.0f;///< temperature devider for 1°C
    float ULTRASOUND_FRONT_DEVIDER  = 1.0f;///< front ultrasound distance devider for 1cm
    float ULTRASOUND_BACK_DEVIDER   = 1.0f;///< back ultrasound distance devider for 1cm

    int SPEED_MAX                   = 127;///< maximum speed (not dependent on driving direction)
    int VAL_SPEED_MAX               =  SPEED_MAX;///< maximum speed forwards
    int VAL_SPEED_MIN               = -SPEED_MAX;///< maximum speed backwards

    int STEER_MAX                   = 127;///< maximum steering (not dependent on steering direction)
    int VAL_STEER_MAX               =  STEER_MAX;///< maximum steering position right
    int VAL_STEER_MIN               = -STEER_MAX;///< maximum steering position left

    void setSerialState(ConnectionState state);
    void setSerialName(String name);
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
