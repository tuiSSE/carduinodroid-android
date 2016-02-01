package tuisse.carduinodroid_android.data;

import android.graphics.drawable.LayerDrawable;

/**
 * Created by mate on 01.02.2016.
 */
public interface Serial {

    void setSerialState(ConnectionState state);
    void setSerialName(String s);
    void setSerialType(SerialType type);

    ConnectionState getSerialState();
    String getSerialName();
    SerialType getSerialType();
    LayerDrawable getSerialConnLogoId(SerialType serialPref);
}
