package tuisse.carduinodroid_android;

/**
 * Created by mate on 08.02.2016.
 */
public class Constants {
    public interface ACTION {
        String EXIT = "tuisse.carduinodroid_android.action.exit";
        String CONTROL_MODE_CHANGED = "tuisse.carduinodroid_android.action.control_mode_changed";
    }

    public interface EVENT {
        String SERIAL_STATUS_CHANGED ="tuisse.carduinodroid_android.event.serial_status_changed";
        String IP_STATUS_CHANGED ="tuisse.carduinodroid_android.event.ip_status_changed";
        String SERIAL_DATA_RECEIVED = "tuisse.carduinodroid_android.event.serial_data_received";
        String IP_DATA_RECEIVED = "tuisse.carduinodroid_android.event.ip_data_received";
    }

    public interface PERMISSION{
        String USB = "tuisse.carduinodroid_android.permission.usb";
    }

    public interface NOTIFICATION_ID{
        int WATCHDOG = 1337;
    }

    public interface DELAY{
        int WATCHDOG    = 3000;     //ms
        int SERIAL      = 100;      //ms
        int IP          = 100;      //ms
        int CAMERA      = 1000/25;  //ms
    }

    public interface TIMEOUT{
        int SERIAL      = 1300;      //ms
        int IP          = 200;       //ms
    }

    public interface HEART_BEAT{
        //put -1 to disable heartbeat log messages
        int WATCHDOG    = 1;    //*DELAY.WATCHDOG
        int SERIAL      = 100;  //*DELAY.SERIAL
        int IP          = 100;  //*DELAY.IP
        int CAMERA      = 100;  //*DELAY.CAMERA
    }




    public interface GESTURE_ANGLE{
        int STEER = 20;//°
        int SPEED = 20;//°
    }

    public interface USB_VENDOR_ID{
        int ARDUINO = 0x2341;
    }

    public interface USB_PRODUCT_ID{
        int ARDUINO_UNO = 0x01;
        int ARDUINO_UNO_R3 = 0x43;
        int ARDUINO_MEGA_2560 = 0x10;
        int ARDUINO_MEGA_2560_R3 = 0x42;
        int ARDUINO_MEGA_2560_ADK = 0x3F;
        int ARDUINO_MEGA_2560_ADK_R3 = 0x44;
    }
}
