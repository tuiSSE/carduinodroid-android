package tuisse.carduinodroid_android;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;

    public SerialUsb(SerialService s) {
        super(s);
        getSerialData().setSerialType(SerialType.USB);
    }

    @Override
    protected boolean find() {
        if (isIdle()) {
            setSerialState(ConnectionState.TRYFIND);
        }
        /*
        //try to connect to usb device
        usbManager = (UsbManager) serialService.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Log.e(TAG, "No UsbManager available");
            setSerialState(ConnectionState.ERROR);
        } else {
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            usbDevice = null;
            while (deviceIterator.hasNext()){
                UsbDevice tempUsbDevice = deviceIterator.next();

                // Print device information. If you think your device should be able
                // to communicate with this app, add it to accepted products below.
                Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
                Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
                Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
                Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
                Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
                Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
                Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
                Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

                if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                    switch (tempUsbDevice.getProductId()) {
                        case ARDUINO_UNO_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_uno));
                            usbDevice = tempUsbDevice;
                            break;
                        case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_uno_r3));
                            usbDevice = tempUsbDevice;
                            break;
                        case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_mega));
                            usbDevice = tempUsbDevice;
                            break;
                        case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_mega_r3));
                            usbDevice = tempUsbDevice;
                            break;
                        case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_mega_adk));
                            usbDevice = tempUsbDevice;
                            break;
                        case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                            getSerialData().setSerialName(serialService.getString(R.string.arduino_mega_adk_r3));
                            usbDevice = tempUsbDevice;
                            break;
                    }
                }
            }
            if(usbDevice == null){
                Log.e(TAG, serialService.getString(R.string.no_arduino) + " found");
                getSerialData().setSerialName(serialService.getString(R.string.no_arduino));
                setSerialState(ConnectionState.ERROR);
            }
            else{
                Log.d(TAG, getSerialData().getSerialName() + " found");
                setSerialState(ConnectionState.FOUND);
            }
        }
        */
        return isFound();
    }

    @Override
    protected boolean connect() {
        if (!isError()) {
            setSerialState(ConnectionState.TRYCONNECT);
        }


        if(!usbManager.hasPermission(usbDevice)){
            UsbReciever usbReciever = new UsbReciever();
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(serialService, 0, new Intent(
                    serialService.getString(R.string.USB_PERMISSION)), 0);
            IntentFilter filter = new IntentFilter(serialService.getString(R.string.USB_PERMISSION));
            serialService.registerReceiver(usbReciever, filter);
            usbManager.requestPermission(usbDevice, mPermissionIntent);
            if(!usbManager.hasPermission(usbDevice)) {
                Log.e(TAG, "usbManager has no permission");
                setSerialState(ConnectionState.ERROR);
                return false;
            }
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
            Log.e(TAG, "No Usb rx endpoint found!");
            setSerialState(ConnectionState.ERROR);
            usbConnection.close();
            return false;
        }
        if (usbEndpointTx == null) {
            Log.e(TAG, "No Usb tx endpoint found!");
            setSerialState(ConnectionState.ERROR);
            usbConnection.close();
            return false;
        }
        setSerialState(ConnectionState.CONNECTED);
        return isConnected();
    }

    @Override
    protected boolean close() {
        if(!isError()){
            getSerialData().setSerialName("");
        }
        if(isRunning()){
            setSerialState(ConnectionState.IDLE);
        }
        else if(!isError()){
            setSerialState(ConnectionState.ERROR);
        }
        if (usbConnection != null) {
            //usbConnection.releaseInterface(?);
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

    class UsbReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (serialService.getString(R.string.USB_PERMISSION).equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            Log.d(TAG, "no usb device");
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    // call your method that cleans up and closes communication with the device
                    Log.d(TAG, "disconnecting from usb device");
                    serialService.stopSelf();
                }
            }
        }
    }
}
