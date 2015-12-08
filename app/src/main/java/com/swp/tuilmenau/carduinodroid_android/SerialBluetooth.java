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
public class SerialBluetooth {
    private final String TAG = "CarduinoSerialBluetooth";
    final static int BUFFERSIZE = 100;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket mmSocket = null;
    BluetoothDevice mmDevice = null;
    OutputStream mmOutputStream = null;
    InputStream mmInputStream = null;
    byte[] readBuffer = null;
    byte[] writeBuffer = null;

    public void find(){

        // Verbindung mit Bluetooth-Adapter herstellen
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "No bluetooth adapter available");
        }else{
            Log.d(TAG, "Bluetooth-Adapter ist bereit");
        }
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }

    public boolean connect(){
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        boolean isConnected = true;


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices) {
                if (device.getName().contains("HC-06")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        mBluetoothAdapter.cancelDiscovery();
        if(mmDevice == null){
            Log.e(TAG,"No paired Ardunio found!");
            isConnected = false;
        }

        // Socket erstellen
        try{
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
            isConnected = false;
            Log.e(TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
        }
        // Socket verbinden
        try {
            mmSocket.connect();
            Log.d(TAG, "Socket verbunden");
        } catch (IOException e) {
            isConnected = false;
            Log.e(TAG, "Socket kann nicht verbinden: " + e.toString());
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (!isConnected) {
            try {
                mmSocket.close();
            } catch (Exception e) {
                Log.e(TAG,"Socket kann nicht beendet werden: " + e.toString());
            }
        }

        // Outputstream erstellen:
        try {
            mmOutputStream = mmSocket.getOutputStream();
            Log.d(TAG, "OutputStream erstellt");
        } catch (IOException e) {
            Log.e(TAG, "OutputStream Fehler: " + e.toString());
            isConnected = false;
        }

        // Inputstream erstellen
        try {
            mmInputStream = mmSocket.getInputStream();
            Log.d(TAG, "InputStream erstellt");
        } catch (IOException e) {
            Log.e(TAG, "InputStream Fehler: " + e.toString());
            isConnected = false;
        }

        if (isConnected) {
            Log.e(TAG, "Verbunden mit " + mmDevice.getName());
        } else {
            Log.e(TAG,"Verbindungsfehler mit " + uuid.toString());
        }
        return isConnected;
    }

    public boolean close(){
        boolean isClosed = true;
        try {
            mmOutputStream.flush();
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG,"Fehler beim Beenden des Streams und Schliessen des Sockets: "+ e.toString());
            isClosed = false;
        }
        mBluetoothAdapter = null;
        mmSocket = null;
        mmDevice = null;
        mmOutputStream = null;
        mmInputStream = null;
        return isClosed;
    }

    public boolean send(){

        return true;
    }

    public boolean rec(){

        return true;
    }
}
