package tuisse.carduinodroid_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.SerialType;

import static tuisse.carduinodroid_android.data.Utils.byteArrayToHexString;

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

    public SerialBluetooth(SerialService s) {
        super(s);
        if(getData() != null) {
            getData().setSerialType(SerialType.BLUETOOTH);
        }
        else{
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
        }
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
            getDataHandler().setBluetoothEnabled(mBluetoothAdapter.isEnabled());
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, serialService.getString(R.string.serialBluetoothEnable));
                serialService.sendToast(serialService.getString(R.string.serialBluetoothEnable));
                mBluetoothAdapter.enable();

            }
            while(mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
                if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF){
                    break;
                }
                try{
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    break;
                }
            }
            if (mBluetoothAdapter.isDiscovering()) {
                Log.d(TAG, "stop bluetooth discovery");
                mBluetoothAdapter.cancelDiscovery();
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                if (pairedDevices.size() > 0) {

                    if (getDataHandler().getBluetoothDeviceName().equals("")) {
                        //start preferences
                        serialService.sendToast(R.string.serialErrorNoBluetoothDeviceChosen);
                        setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoBluetoothDeviceChosen);
                        return false;
                    }

                    int foundDevices = 0;
                    for (BluetoothDevice device : pairedDevices) {
                        Log.d(TAG, "bluetooth device found: " + device.getName());
                        if (device.getName().contains(getDataHandler().getBluetoothDeviceName())) {
                            mmDevice = device;
                            Log.d(TAG, "carduinodroid device found: " + mmDevice.getName());
                            Log.d(TAG, mmDevice.getAddress());
                            foundDevices++;
                        }
                    }
                    if(foundDevices == 1){
                        getData().setSerialName(mmDevice.getName());
                        setSerialState(ConnectionEnum.FOUND);
                    }
                    else if(foundDevices > 1){
                        mmDevice = null;
                        setSerialState(ConnectionEnum.TRYCONNECTERROR,String.format(
                                serialService.getString(R.string.serialErrorTooMuchCarduinoBluetoothDeviceFound),
                                foundDevices,
                                getDataHandler().getBluetoothDeviceName()
                        ));
                        return false;
                    }
                    else {
                        //foundDevice == 0
                        setSerialState(ConnectionEnum.TRYCONNECTERROR, String.format(
                                serialService.getString(R.string.serialErrorNoCarduinoBluetoothDeviceFound),
                                pairedDevices.size(),
                                getDataHandler().getBluetoothDeviceName()
                        ));
                        return false;
                    }
                } else {
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
            Log.i(TAG, "connected with " + getData().getSerialName());
        } else {
            Log.e(TAG, "connection error: " + uuid.toString());
        }
        return isConnected();
    }

    @Override
    public boolean close() {
        try {
            //getSerialData().setSerialName(serialService.getString(R.string.serialDeviceNone));
            Log.d(TAG,"Closing serial connection");
            if(isRunning()){
                setSerialState(ConnectionEnum.IDLE);
            }
            else if(isUnknown()){
                Log.d(TAG,serialService.getString(R.string.serialErrorUnused));
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
            Log.d(TAG,"bluetoothHandling: " + getDataHandler().getBluetoothHandling().toString());
            switch (getDataHandler().getBluetoothHandling()) {
                case ON:
                    break;
                case OFF:
                    mBluetoothAdapter.disable();
                    break;
                default://AUTO
                    if (!getDataHandler().getBluetoothEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    break;
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
        //Log.d(TAG, byteArrayToHexString(getDataHandler().serialFrameAssembleTx()));
        mmOutputStream.write(getDataHandler().serialFrameAssembleTx());
    }

    @Override
    protected int receive() throws IOException {
        final int BUFFER_LENGTH = RECEIVE_BUFFER_LENGTH;
        int acceptedFrame = 0;
        while(mmInputStream.available()>0){
            byte[] buffer = new byte[BUFFER_LENGTH];
            int len = mmInputStream.read(buffer,0,BUFFER_LENGTH);
            for(int i = 0; i < len; i++){
                if(getDataHandler().serialFrameAppendRx(buffer[i])){
                    acceptedFrame++;
                }
            }
            //Log.d(TAG, byteArrayToHexString(buffer));
        }
        return acceptedFrame;
    }
}
