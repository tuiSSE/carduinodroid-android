package tuisse.carduinodroid_android.data;

/**
 * Created by keX on 04.01.2016.
 */
public enum IpType {
    //// TODO: 12.01.2016 support mobile internet?!
    NONE(0),WLAN(1),MOBILE(2);
    IpType(int t){
        type = t;
    }
    int type;
}
