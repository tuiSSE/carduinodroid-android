package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 01.02.2016.
 */
public interface Ip {

    LayerDrawable getIpConnLogoId();

    String getRemoteIp();
    String getTransceiverIp();
    ConnectionState getIpState();
    IpType getIpType();

    void setIpState(ConnectionState is);
    void setIpType(IpType it);
}
