package tuisse.carduinodroid_android.data;

/**
 * <h1>Serial Type enum</h1>
 * Enumeration which abstractly codes the serial connection type
 *
 * @author Till Max Schwikal
 * @since 04.01.2016
 * @version 1.0
 *
 * @see tuisse.carduinodroid_android.data.CarduinoData
 */
public enum SerialType {
    /**
     * no serial connection preferred (should not happen)
     */
    NONE(0),
    /**
     * bluetooth serial connection should be used
     */
    BLUETOOTH(1),
    /**
     * usb connection should be used
     */
    USB(2),
    /**
     * automatically choose usb or bluetooth connection
     * AUTO type is not implemented yet.
     */
    AUTO(3);

    SerialType(int t){
        type = t;
    }
    int type;

    /**
     * conversion function serial type from integer
     * @param x integer to be converted to a serial type
     * @return serial type converted from integer
     */
    public static SerialType fromInteger(int x) {
        switch(x) {
            case 1:
                return BLUETOOTH;
            case 2:
                return USB;
            case 3:
                return AUTO;
            default:
                return NONE;
        }
    }

    /**
     * conversion function serial type to integer
     * @param st serial type tp be converted to a integer
     * @return integer converted from serial type
     */
    public static Integer toInteger(SerialType st){
        switch (st){
            case BLUETOOTH:
                return 1;
            case USB:
                return 2;
            case AUTO:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * boolean check function
     * @return true if serial type is BLUETOOTH or AUTO
     */
    public boolean isAutoBluetooth(){
        return this == BLUETOOTH || this == AUTO;
    }

    /**
     * boolean check function
     * @return true if serial type is BLUETOOTH
     */
    public boolean isBluetooth(){
        return this == BLUETOOTH;
    }
    /**
     * boolean check function
     * @return true if serial type is USB or AUTO
     */
    public boolean isAutoUsb(){
        return this == USB || this == AUTO;
    }
    /**
     * boolean check function
     * @return true if serial type is USB
     */
    public boolean isUsb(){
        return this == USB;
    }
    /**
     * boolean check function
     * @return true if serial type is AUTO
     */
    public boolean isAuto(){
        return this == AUTO;
    }
    /**
     * boolean check function
     * @return true if serial type is NONE
     */
    public boolean isNone(){
        return this == NONE;
    }
}
