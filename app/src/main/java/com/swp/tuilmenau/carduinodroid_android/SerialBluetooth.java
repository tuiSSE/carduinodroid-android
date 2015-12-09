package com.swp.tuilmenau.carduinodroid_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
    final static int BUFFERSIZE = 100;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice mmDevice = null;
    BluetoothSocket mmSocket = null;
    OutputStream mmOutputStream = null;
    InputStream mmInputStream = null;

    @Override
    public boolean find(){
        if(isIdle())
            state = State.TRYCONNECT;
        // Verbindung mit Bluetooth-Adapter herstellen
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "No bluetooth adapter available");
            state = State.ERROR;
        }else{
            Log.d(TAG, "Bluetooth-Adapter ist bereit");
        }
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
        return isTryConnect();
    }

    @Override
    public boolean connect() {
        if(!isError())
            state = State.TRYCONNECT;
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
            state = State.ERROR;
        }

        if(isTryConnect()) {
            // Socket erstellen
            try {
                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
                state = State.ERROR;
                Log.e(TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
            }
            mBluetoothAdapter.cancelDiscovery();
            // Socket verbinden
            try {
                mmSocket.connect();
            } catch (IOException e) {
                state = State.ERROR;
                Log.e(TAG, "Socket kann nicht verbinden: " + e.toString());
            }
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (isError()) {
            try {
                mmSocket.close();
                Log.d(TAG,"Socket closed");
            } catch (Exception e) {
                Log.e(TAG,"Socket kann nicht beendet werden: " + e.toString());
            }
        }
        else {
            // Outputstream erstellen:
            try {
                mmOutputStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "OutputStream Fehler: " + e.toString());
                state = State.STREAMERROR;
            }

            // Inputstream erstellen
            try {
                mmInputStream = mmSocket.getInputStream();
                state = State.CONNECTED;
            } catch (IOException e) {
                Log.e(TAG, "InputStream Fehler: " + e.toString());
                state = State.STREAMERROR;
            }
        }

        if (isConnected()) {
            Log.i(TAG, "Verbunden mit " + mmDevice.getName());
        } else {
            Log.e(TAG,"Verbindungsfehler mit " + uuid.toString());
        }
        return isConnected();
    }

    @Override
    public boolean close(){
        try {
            mmOutputStream.flush();
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            state = State.IDLE;
        } catch (IOException e) {
            Log.e(TAG,"Fehler beim Beenden des Streams und Schliessen des Sockets: "+ e.toString());
            state = State.ERROR;
        }
        mBluetoothAdapter = null;
        mmSocket = null;
        mmDevice = null;
        mmOutputStream = null;
        mmInputStream = null;
        return isIdle();
    }

    @Override
    public void send(byte[] buffer) {

    }

    @Override
    public byte[] receive() {
        return new byte[0];
    }
}
