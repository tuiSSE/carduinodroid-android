package com.swp.tuilmenau.carduinodroid_android;

import android.app.Application;
import android.util.Log;

/**
 * Created by keX on 04.12.2015.
 */
public class CarduinodroidApplication extends Application {
    private static final String TAG = "CarduinoApplication";
    private boolean serialServiceRunning = false;
    private SerialDataTx serialDataTx;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreated");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "onTerminated");
    }

    public boolean isSerialServiceRunning() {
        return this.serialServiceRunning;
    }

    public void setSerialServiceRunning(boolean serialServiceRunning) {
        this.serialServiceRunning = serialServiceRunning;
    }

    private class SerialDataTx {
        private final int bufferLength = 4;
        //byte 1
        private int version     = 1;
        private int length      = 3;
        //byte 2
        private int speedVal    = 0;
        private int speedDir    = 1;
        //byte 3
        private int steerVal    = 0;
        private int steerDir    = 0;
        //byte 4
        private int led         = 0;
        private int light       = 0;
        private int autoStop    = 1;
        private int resetAccCur = 0;

        private final int versionShift      = 6;
        private final int versionMask       = 0xc0;
        private final int lengthShift       = 0;
        private final int lengthMask        = 0x0f;

        private final int speedValShift     = 4;
        private final int speedValMask      = 0xf0;
        private final int speedDirShift     = 3;
        private final int speedDirMask      = 0x08;

        private final int steerValShift     = 4;
        private final int steerValMask      = 0xf0;
        private final int steerDirShift     = 3;
        private final int steerDirMask      = 0x08;

        private final int ledShift          = 7;
        private final int ledMask           = 0x80;
        private final int lightShift        = 6;
        private final int lightMask         = 0x40;
        private final int autoStopShift     = 5;
        private final int autoStopMask      = 0x20;
        private final int resetAccCurShift  = 4;
        private final int resetAccCurMask   = 0x10;

        public byte[] getSerialCommand(){
            byte[] command = new byte[bufferLength];
            command[0] = (byte)(0x00 | ((length   << lengthShift  ) & lengthMask  ) | ((version     << versionShift )    & versionMask ));
            command[1] = (byte)(0x00 | ((speedDir << speedDirShift) & speedDirMask) | ((speedVal    << speedValShift)    & speedValMask));
            command[2] = (byte)(0x00 | ((steerDir << steerDirShift) & steerDirMask) | ((steerVal    << steerValShift)    & steerValMask));
            command[3] = (byte)(0x00 | ((led      << ledShift     ) & ledMask     ) | ((light       << lightShift   )    & lightMask) |
                                       ((autoStop << autoStopShift) & autoStopMask) | ((resetAccCur << resetAccCurShift) & resetAccCurMask));
            return command;
        }

        public void setSpeedDir(int speedDir){
            if(speedDir == 0)
                this.speedDir = 0; //backward
            else
                this.speedDir = 1; //forward
        }
        public void setSpeedVal(int speedVal){
            if((speedVal >= 0) && (speedVal <= 9))
                this.speedVal = speedVal;
        }
    }
}
