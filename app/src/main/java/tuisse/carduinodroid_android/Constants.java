package tuisse.carduinodroid_android;

import android.widget.Toast;

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
        String COMMUNICATION_STATUS_CHANGED ="tuisse.carduinodroid_android.event.communication_status_changed";
        String SERIAL_DATA_RECEIVED = "tuisse.carduinodroid_android.event.serial_data_received";
        String IP_DATA_RECEIVED = "tuisse.carduinodroid_android.event.ip_data_received";
        String CAMERA_DATA_RECEIVED = "tuisse.carduinodroid_android.event.camera_data_received";
        String CAMERA_SUPPORTED_RESOLUTION = "tuisse.carduinodroid_android.event.camera_supported_resolution";
        String CAMERA_SETTINGS_CHANGED = "tuisse.carduinodroid_android.event.camera_settings_changed";
        String SOUND_PLAY_CHANGED = "tuisse.carduinodroid_android.event.sound_play_changed";
        String IP_DATA_CAR = "tuisse.carduinodroid_android.event.ip_data_car";
        String IP_DATA_VIDEO = "tuisse.carduinodroid_android.event.ip_data_video";
        String IP_DATA_CONTROL = "tuisse.carduinodroid_android.event.ip_data_control";
        String IP_DATA_CAMERA = "tuisse.carduinodroid_android.event.ip_data_camera";
        String MOBILITY_GPS_CHANGED = "tuisse.carduinodroid_android.event.mobility_gps_changed";
        String FEATURES_CHANGED = "tuisse.carduinodroid_android.event.features_changed";
    }

    public interface PERMISSION{
        String USB = "tuisse.carduinodroid_android.permission.usb";
    }

    public interface LOG{
        boolean IP_SENDER = false;
        boolean SERIAL = false;
        boolean IP_RECEIVER = false;
    }

    public interface NOTIFICATION_ID{
        int WATCHDOG = 1337;
    }

    public interface DELAY{
        int WATCHDOG    = 3000;     //ms
        int SERIAL      = 50;      //ms
        int IP          = 50;       //ms
        int CONNECTIONTRY = 3000;   //ms
        int FACTOR_CONTROL = 2;     //2 * IP Delay
        int FACTOR_CAR = 2;         //2 * IP Delay
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

    public interface FACTORS{
        float DATA      = 1024;
        float TIME      = 1000;
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

    public interface IP_CONNECTION{
        int DATAPORT = 12020;
        int CTRLPORT = 12021;
        String TAG_DATAPORT = "DataSocket";
        String TAG_CTRLPORT = "CtrlSocket";

        int MAX_PREF_IP = 5;
        String TAG_PREF_IP = "Last IP Connections";
        String PREF_FIRST_IP = "First Value";
        String PREF_SECOND_IP = "Second Value";
        String PREF_THIRD_IP = "Third Value";
        String PREF_FOURTH_IP = "Fourth Value";
        String PREF_FIFTH_IP = "Fifth Value";
        String PREF_IP_NAMES[] = {PREF_FIRST_IP,PREF_SECOND_IP,PREF_THIRD_IP,PREF_FOURTH_IP,PREF_FIFTH_IP};
    }

    public interface JSON_OBJECT{
        int MY_VERSION = 2;

        String TAG_HEADER_VERSION = "Version";
        String TAG_HEADER_INFORMATION_TYPE = "Information Type";
        String TAG_HEADER_DATA_SERVER_STATUS = "Data Server Status";
        String TAG_HEADER = "Header";

        String TAG_CAR_CURRENT = "Current";
        String TAG_CAR_BATTERY_ABSOLUTE = "Battery Absolute";
        String TAG_CAR_BATTERY_PERCENTAGE = "Battery Relative";
        String TAG_CAR_VOLTAGE = "Voltage";
        String TAG_CAR_TEMPERATURE = "Temperature";
        String TAG_CAR_ULTRASONIC_FRONT = "Ultra Sonic Front";
        String TAG_CAR_ULTRASONIC_BACK = "Ultra Sonic Back";
        String TAG_CAR = "Car Information";
        String NUM_CAR = "#Car";

        String TAG_MOBILITY_GPS = "GPS Data";
        String TAG_MOBILITY_WLAN_AVAILABLE = "WLAN Available";
        String TAG_MOBILITY_WLAN_ACTIVE = "WLAN Active";
        String TAG_MOBILITY_MOBILE_AVAILABLE = "Mobile Available";
        String TAG_MOBILITY_MOBILE_ACTIVE = "Mobile Active";
        String TAG_MOBILITY = "Mobilty Information";
        String NUM_MOBILITY = "#Mobility";

        String TAG_FEATURES_BATTERY_PHONE = "Battery Level Phone";
        String TAG_FEATURES_VIBRATION = "Vibration Value";
        String TAG_FEATURES = "Features Data";
        String NUM_FEATURES = "#Features";

        String TAG_HARDWARE_CAMERA_RESOLUTION = "Camera Resolution";
        String TAG_HARDWARE_CAMERA_RESOLUTION_NUM = "Camera Resolution Numbers";
        String TAG_HARDWARE = "Hardware Information";
        String NUM_HARDWARE = "#Hardware";

        String TAG_VIDEO_TYPE = "Video Type";
        String TAG_VIDEO_SOURCE = "Video Source Data";
        String TAG_VIDEO = "Video Data";
        String NUM_VIDEO = "#Video";

        String TAG_SERIAL_STATUS = "Serial State";
        String TAG_SERIAL_ERROR = "Serial Error";
        String TAG_SERIAL_NAME = "Serial Name";
        String TAG_SERIAL_TYPE = "Serial Type";
        String TAG_SERIAL = "Serial Status";
        String NUM_SERIAL = "#Serial";

        String TAG_CONTROL_SPEED = "Car Control Speed";
        String TAG_CONTROL_STEER = "Car Control Steer";
        String TAG_CONTROL_FRONT_LIGHT = "Car Control Light";
        String TAG_CONTROL_STATUS_LED = "Car Control Status LED";
        String TAG_CONTROL = "Car Control";
        String NUM_CONTROL = "#Control";

        String TAG_CAMERA_TYPE = "Camera Type";
        String TAG_CAMERA_RESOLUTION = "Camera Resolution";
        String TAG_CAMERA_LIGHT = "Camera Flashlight";
        String TAG_CAMERA_QUALITY = "Camera Quality";
        String TAG_CAMERA_ORIENTATION = "Camera Orientation";
        String TAG_CAMERA = "Camera Setup";
        String NUM_CAMERA = "#Camera";

        String TAG_SOUND_PLAY = "Sound Play";
        String TAG_SOUND_RECORD = "Sound Record";
        String TAG_SOUND = "Sound Setup";
        String NUM_SOUND = "#Sound";
    }

    public interface CAMERA_VALUES{
        String[] ORIENTATION_DEGREES = {"0","90","180","270"};
        String TAG_PREF_ORIENTATION = "Orientation Camera";
        String ORIENTATION_FRONT = "Orientation Front";
        String ORIENTATION_BACK = "Orientation Back";
        int ORIENTATION_FRONT_INIT = 90;
        int ORIENTATION_BACK_INIT = 270;
    }
}
