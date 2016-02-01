package tuisse.carduinodroid_android.data;

/**
 * Created by mate on 01.02.2016.
 */
public interface SerialTx {
    int SPEED_MAX = 127;
    int STEER_MAX = 127;

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

    byte[] serialGet();

}
