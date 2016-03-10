package tuisse.carduinodroid_android.data;

/**
 * <h1>Ip Type enum</h1>
 *
 * Enumeration which abstractly codes the ip connection type.
 *
 * @author Till Max Schwikal
 * @since 04.01.2016
 * @version 1.0
 */
public enum IpType {
    /// TODO: 12.01.2016 support mobile internet?!
    /**
     * no ip connection is opened
     */
    NONE(0),
    /**
     * wlan ip connection is about to be established
     */
    WLAN(1),
    /**
     * mobile ip connection is about to be established. May produce extra costs
     * (traffic of this application is enormous)
     */
    MOBILE(2);
    IpType(int t){
        type = t;
    }
    int type;
}
