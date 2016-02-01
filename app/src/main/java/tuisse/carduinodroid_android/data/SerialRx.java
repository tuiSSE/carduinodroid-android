package tuisse.carduinodroid_android.data;

/**
 * Created by mate on 01.02.2016.
 */
public interface SerialRx {
    float CURRENT_DEVIDER = 10.0f;
    float ABS_BATT_DEVIDER = 1.0f;
    float PER_BATT_DEVIDER = 1.0f;
    float VOLTAGE_DEVIDER = 10.0f;
    float TEMPERATURE_DEVIDER = 2.0f;
    float ULTRASOUND_FRONT_DEVIDER = 1.0f;
    float ULTRASOUND_BACK_DEVIDER = 1.0f;

    float getCurrent();
    float getAbsoluteBatteryCapacity();
    float getPercentBatteryCapacity();
    float getVoltage();
    float getDs2745Temperature();
    float getUltrasoundFront();
    float getUltrasoundBack();

    boolean serialAppend(byte inChar);
}
