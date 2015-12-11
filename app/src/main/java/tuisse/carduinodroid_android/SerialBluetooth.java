package tuisse.carduinodroid_android;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by keX on 08.12.2015.
 */
public class SerialBluetooth extends SerialConnection {
    private final String TAG = "CarduinoSerialBluetooth";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mmDevice = null;
    private BluetoothSocket mmSocket = null;
    private OutputStream mmOutputStream = null;
    private InputStream mmInputStream = null;

    public SerialBluetooth(Application a) {
        super(a);
    }

    @Override
    public boolean find() {
        if (isIdle())
            serialState = SerialState.TRYCONNECT;
        // Verbindung mit Bluetooth-Adapter herstellen
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "No bluetooth adapter available");
            serialState = SerialState.ERROR;
        } else {
            Log.d(TAG, "Bluetooth-Adapter ist bereit");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        return isTryConnect();
    }

    @Override
    public boolean connect() {
        if (!isError()) {
            serialState = SerialState.TRYCONNECT;
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().contains("HC-06")) {
                    mmDevice = device;
                    break;
                }
            }
        }

        if (mmDevice == null) {
            Log.e(TAG, "No paired Ardunio found!");
            serialState = SerialState.ERROR;
        }

        if (isTryConnect()) {
            // Socket erstellen
            try {
                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
                serialState = SerialState.ERROR;
                Log.e(TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
            }
            mBluetoothAdapter.cancelDiscovery();
            // Socket verbinden
            try {
                mmSocket.connect();
            } catch (IOException e) {
                serialState = SerialState.ERROR;
                Log.e(TAG, "Socket kann nicht verbinden: " + e.toString());
            }
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (isError()) {
            try {
                mmSocket.close();
                Log.d(TAG, "Socket closed");
            } catch (Exception e) {
                Log.e(TAG, "Socket kann nicht beendet werden: " + e.toString());
            }
        } else {
            // Outputstream erstellen:
            try {
                mmOutputStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "OutputStream Fehler: " + e.toString());
                serialState = SerialState.STREAMERROR;
            }

            // Inputstream erstellen
            try {
                mmInputStream = mmSocket.getInputStream();
                serialState = SerialState.CONNECTED;
            } catch (IOException e) {
                Log.e(TAG, "InputStream Fehler: " + e.toString());
                serialState = SerialState.STREAMERROR;
            }
        }

        if (isConnected()) {
            Log.i(TAG, "Verbunden mit " + mmDevice.getName());
        } else {
            Log.e(TAG, "Verbindungsfehler mit " + uuid.toString());
        }
        return isConnected();
    }

    @Override
    public boolean close() {
        try {
            serialState = SerialState.IDLE;
            if (mmOutputStream != null) {
                mmOutputStream.flush();
                mmOutputStream.close();
            }
            if (mmInputStream != null) {
                mmInputStream.close();
            }
            if (mmSocket != null) {
                mmSocket.close();
            }

        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Beenden des Streams und Schliessen des Sockets: " + e.toString());
            serialState = SerialState.ERROR;
        }
        mBluetoothAdapter = null;
        mmSocket = null;
        mmDevice = null;
        mmOutputStream = null;
        mmInputStream = null;
        return isIdle();
    }

    @Override
    protected void send() throws IOException {
        Log.d(TAG,carduino.dataContainer.serialDataTx.byteArrayToHexString(carduino.dataContainer.serialDataTx.get()));
        mmOutputStream.write(carduino.dataContainer.serialDataTx.get());
    }

    @Override
    protected void receive() throws IOException {
        byte[] buffer = new byte[20];
       mmInputStream.read(buffer);
        Log.d(TAG, carduino.dataContainer.serialDataTx.byteArrayToHexString(buffer));
    }
}
