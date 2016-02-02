package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 02.02.2016.
 */
public interface CarduinoDroidIF extends CarduinoIF {

    LayerDrawable getIpConnLogoId();
    String getRemoteIp();
    String getTransceiverIp();
    ConnectionState getIpState();
    IpType getIpType();

    void setIpState(ConnectionState is);
    void setIpType(IpType it);
}
