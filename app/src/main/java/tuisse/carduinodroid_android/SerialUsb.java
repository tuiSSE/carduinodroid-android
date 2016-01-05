package tuisse.carduinodroid_android;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by keX on 04.01.2016.
 */
public class SerialUsb extends SerialConnection {
    private final String TAG = "CarduinoSerialUsb";
    private UsbManager usbManager = null;
    private UsbDevice usbDevice = null;
    private UsbDeviceConnection usbConnection = null;
    private UsbEndpoint usbEndpointRx = null;
    private UsbEndpoint usbEndpointTx = null;

    public SerialUsb(SerialService s) {
        super(s);
        getSerialData().setSerialType(SerialType.USB);
    }

    @Override
    protected boolean find() {
        if (isIdle()) {
            setSerialState(ConnectionState.TRYCONNECT);
        }
        //try to connect to usb device
        usbManager = (UsbManager) serialService.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Log.e(TAG, "No UsbManager available");
            setSerialState(ConnectionState.ERROR);
        } else {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            while(deviceIterator.hasNext()){
                usbDevice = deviceIterator.next();
                Log.e(TAG, usbDevice.toString());
                //your code
            }
            if(usbDevice == null){
                Log.e(TAG, "No USB device connected!");
                setSerialState(ConnectionState.ERROR);
            }
            else{
                Log.d(TAG, "UsbDevice is ready");
            }
        }
        return isTryConnect();
    }

    @Override
    protected boolean connect() {
        if (!isError()) {
            setSerialState(ConnectionState.TRYCONNECT);
        }
        usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Opening USB device failed!");
            setSerialState(ConnectionState.ERROR);
            return false;
        }
        UsbInterface usbInterface = usbDevice.getInterface(1);
        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Claiming interface failed!");
            setSerialState(ConnectionState.ERROR);
            usbConnection.close();
            return false;
        }
        // Arduino USB serial converter setup
        // Set control line state
        usbConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
        // Set line encoding.
        usbConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(9600), 7, 0);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    usbEndpointRx = usbInterface.getEndpoint(i);
                } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                    usbEndpointTx = usbInterface.getEndpoint(i);
                }
            }
        }
        if (usbEndpointRx == null) {
            Log.e(TAG, "No Usb Rx endpoint found!");
            setSerialState(ConnectionState.ERROR);
            usbConnection.close();
            return false;
        }
        if (usbEndpointTx == null) {
            Log.e(TAG, "No Usb Tx endpoint found!");
            setSerialState(ConnectionState.ERROR);
            usbConnection.close();
            return false;
        }
        setSerialState(ConnectionState.CONNECTED);
        return true;
    }

    @Override
    protected boolean close() {
        if(!isError()){
            getSerialData().setSerialName("");
        }
        if(isRunning()){
            setSerialState(ConnectionState.IDLE);
        }
        if (usbConnection != null) {
            usbConnection.close();
            usbConnection = null;
        }
        usbManager = null;
        usbDevice = null;
        usbEndpointTx = null;
        usbEndpointRx = null;
        return isIdle();
    }


    @Override
    protected void send() throws IOException {
        final byte[] buffer = getSerialData().serialTx.get();
        final int len = usbConnection.bulkTransfer(usbEndpointTx, buffer, buffer.length, 0);
    }

    @Override
    protected int receive() throws IOException {
        final int BUFFER_LENGTH = 20;
        int acceptedFrame = 0;
        byte[] buffer = new byte[BUFFER_LENGTH];
        int len = usbConnection.bulkTransfer(usbEndpointRx, buffer, buffer.length, TIMEOUT);
        if (len > 0) {
            for(int i = 0; i < len; i++){
                if(getSerialData().serialRx.append(buffer[i])){
                    acceptedFrame++;
                };
            }
            //Log.d(TAG, getSerialData().serialTx.byteArrayToHexString(buffer));
        }
        return acceptedFrame;
    }


    private byte[] getLineEncoding(int baudRate) {
        final byte[] lineEncoding = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        //Get the least significant byte of baudRate,
        //and put it in first byte of the array being sent
        lineEncoding[0] = (byte)(baudRate & 0xFF);
        //Get the 2nd byte of baudRate,
        //and put it in second byte of the array being sent
        lineEncoding[1] = (byte)((baudRate >> 8) & 0xFF);
        //ibid, for 3rd byte (my guess, because you need at least 3 bytes
        //to encode your 115200+ settings)
        lineEncoding[2] = (byte)((baudRate >> 16) & 0xFF);
        return lineEncoding;
    }
}
