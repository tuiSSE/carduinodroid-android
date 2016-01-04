package tuisse.carduinodroid_android;

import android.app.Application;

import java.io.IOException;

/**
 * Created by keX on 04.01.2016.
 */
public class SerialUsb extends SerialConnection {

    public SerialUsb(SerialService s) {
        super(s);
        getSerialData().setSerialType(SerialType.USB);
    }

    @Override
    protected boolean find() {
        return false;
    }

    @Override
    protected boolean connect() {
        return false;
    }

    @Override
    protected boolean close() {
        return false;
    }

    @Override
    protected void send() throws IOException {

    }

    @Override
    protected int receive() throws IOException {
        return 0;
    }
}
