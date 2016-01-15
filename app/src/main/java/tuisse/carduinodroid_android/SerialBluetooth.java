package tuisse.carduinodroid_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.widget.Toast;

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
    //private FindBluetoothBroadcastReceiver mReceiver = null;

    public SerialBluetooth(SerialService s) {
        super(s);
        getSerialData().setSerialType(SerialType.BLUETOOTH);
    }

    @Override
    public boolean find() {
        if (isIdle()) {
            setSerialState(ConnectionEnum.TRYFIND);
            // Try to get connection with Bluetooth-Adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, serialService.getString(R.string.serialErrorNoBluetoothAdapter));
                setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoBluetoothAdapter);
                return false;
            } else {
                Log.d(TAG, "Bluetooth adapter is ready");
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, serialService.getString(R.string.serialBluetoothEnable));
                serialService.sendToast(serialService.getString(R.string.serialBluetoothEnable));
                mBluetoothAdapter.enable();
            }
            if (mBluetoothAdapter.isDiscovering()) {
                Log.d(TAG, "stop bluetooth discovery");
                mBluetoothAdapter.cancelDiscovery();
            }
            //if no bluetooth device is registered add it
            Log.d(TAG, "+" + serialService.getCarduino().dataContainer.preferences.getBluetoothDeviceName() + "+");
            Log.d(TAG, "+" + serialService.getString(R.string.serialDefaultBluetoothDeviceName) + "+");

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                if (pairedDevices.size() > 0) {

                    if (serialService.getCarduino().dataContainer.preferences.getBluetoothDeviceName().equals("")) {
                        //start preferences
                        Log.d(TAG, serialService.getString(R.string.serialErrorNoBluetoothDeviceChosen));
                        serialService.sendToast(R.string.serialErrorNoBluetoothDeviceChosen);
                        setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoBluetoothDeviceChosen);
                        return false;
                    }

                    for (BluetoothDevice device : pairedDevices) {
                        Log.d(TAG, "bluetooth device found: " + device.getName());
                        if (device.getName().contains(serialService.getCarduino().dataContainer.preferences.getBluetoothDeviceName())) {
                            mmDevice = device;
                            Log.d(TAG, "carduinodroid device found: " + mmDevice.getName());
                            getSerialData().setSerialName(mmDevice.getName());
                            setSerialState(ConnectionEnum.FOUND);
                            break;
                        }
                    }
                    if (!isFound()) {
                        Log.e(TAG, serialService.getString(R.string.serialErrorNoCarduinoBluetoothDeviceFound));
                        setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoCarduinoBluetoothDeviceFound);
                        return false;
                    }
                } else {
                    Log.e(TAG, serialService.getString(R.string.serialErrorNoBluetoothDevicePaired));
                    setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoBluetoothDevicePaired);
                    return false;
                }
            }
        }
        else{
            Log.e(TAG, serialService.getString(R.string.serialErrorNotIdle));
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNotIdle);
        }
        return isFound();
    }

    @Override
    public boolean connect() {
        if (!isError()) {
            setSerialState(ConnectionEnum.TRYCONNECT);
        }

        if (mmDevice == null) {
            Log.e(TAG, serialService.getString(R.string.serialErrorNoBluetoothDevicePaired));
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoBluetoothDevicePaired);
        }

        final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        if (isTryConnect()) {
            // Socket erstellen
            try {
                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
                setSerialState(ConnectionEnum.TRYCONNECTERROR, String.format(serialService.getString(R.string.serialErrorSocketCreation),e.toString()));
                Log.e(TAG, String.format(serialService.getString(R.string.serialErrorSocketCreation),e.toString()));
            }
            mBluetoothAdapter.cancelDiscovery();
            // Socket verbinden
            try {
                mmSocket.connect();
            } catch (IOException e) {
                setSerialState(ConnectionEnum.TRYCONNECTERROR, String.format(serialService.getString(R.string.serialErrorSocketConnect),e.toString()));
                Log.e(TAG, String.format(serialService.getString(R.string.serialErrorSocketConnect),e.toString()));
            }
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (isError()) {
            try {
                mmSocket.close();
                Log.d(TAG, "Socket closed");
            } catch (Exception e) {
                Log.e(TAG, String.format(serialService.getString(R.string.serialErrorSocketClose),e.toString()));
            }
        } else {
            // Outputstream erstellen:
            try {
                mmOutputStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, String.format(serialService.getString(R.string.serialErrorOutputStream),e.toString()));
                setSerialState(ConnectionEnum.STREAMERROR, String.format(serialService.getString(R.string.serialErrorOutputStream),e.toString()));
            }

            // Inputstream erstellen
            if(!isError()) {
                try {
                    mmInputStream = mmSocket.getInputStream();
                    setSerialState(ConnectionEnum.CONNECTED);
                } catch (IOException e) {
                    Log.e(TAG, "InputStream error: " + e.toString());
                    setSerialState(ConnectionEnum.STREAMERROR, String.format(serialService.getString(R.string.serialErrorInputStream),e.toString()));
                }
            }
        }

        if (isConnected()) {
            Log.i(TAG, "connected with " + getSerialData().getSerialName());
        } else {
            Log.e(TAG, "connection error: " + uuid.toString());
        }
        return isConnected();
    }

    @Override
    public boolean close() {
        try {
            //getSerialData().setSerialName(serialService.getString(R.string.serialDeviceNone));
            if(isRunning()){
                setSerialState(ConnectionEnum.IDLE);
            }
            else if(isUnknown()){
                Log.d(TAG,serialService.getString(R.string.serialErrorIsUnknown));
            }
            else if(!isError()){
                setSerialState(ConnectionEnum.ERROR, R.string.serialErrorUnexpectedClose);
            }
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
            Log.e(TAG, String.format(serialService.getString(R.string.serialErrorClose),e.toString()));
            setSerialState(ConnectionEnum.ERROR,String.format(serialService.getString(R.string.serialErrorClose),e.toString()));
        }
        if(mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter = null;
        }
        mmSocket = null;
        mmDevice = null;
        mmOutputStream = null;
        mmInputStream = null;
        return isIdle();
    }

    @Override
    protected void send() throws IOException {
        Log.d(TAG, getSerialData().serialTx.byteArrayToHexString(getSerialData().serialTx.get()));
        mmOutputStream.write(getSerialData().serialTx.get());
    }

    @Override
    protected int receive() throws IOException {
        final int BUFFER_LENGTH = RECEIVE_BUFFER_LENGTH;
        int acceptedFrame = 0;
        while(mmInputStream.available()>0){
            byte[] buffer = new byte[BUFFER_LENGTH];
            int len = mmInputStream.read(buffer,0,BUFFER_LENGTH);
            for(int i = 0; i < len; i++){
                if(getSerialData().serialRx.append(buffer[i])){
                    acceptedFrame++;
                };
            }
            Log.d(TAG, getSerialData().serialTx.byteArrayToHexString(buffer));
        }
        return acceptedFrame;
    }
/*
    private class FindBluetoothBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                Log.i(TAG, "bluetooth device found");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a Toast
                String derp = device.getName() + " - " + device.getAddress();
                Toast.makeText(context, derp, Toast.LENGTH_LONG);
            }
        }
    }
    */
}
