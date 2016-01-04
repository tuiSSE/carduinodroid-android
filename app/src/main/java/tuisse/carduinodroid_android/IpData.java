package tuisse.carduinodroid_android;

/**
 * Created by keX on 04.01.2016.
 */
public class IpData {
    ConnectionState ipState;
    IpType ipType;

    public IpData(){
        ipState = ConnectionState.IDLE;
        ipType = IpType.NONE;

    }
}
