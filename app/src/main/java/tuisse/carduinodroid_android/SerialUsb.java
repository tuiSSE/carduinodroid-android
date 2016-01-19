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
    private UsbInterface usbInterface = null;

    private final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;

    public SerialUsb(SerialService s) {
        super(s);
        if(getSerialData() != null) {
            getSerialData().setSerialType(SerialType.USB);
        }
        else{
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
        }
    }

    @Override
    protected boolean find() {
        if (isIdle()) {
            setSerialState(ConnectionEnum.TRYFIND);
            //try to connect to usb device
            if(usbManager == null) {
                usbManager = (UsbManager) serialService.getSystemService(Context.USB_SERVICE);
            }
            if (usbManager != null){
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                if (deviceList != null) {
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    //usbDevice = null;
                    while (deviceIterator.hasNext()) {
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
                                default:
                                    break;
                            }
                        }
                    }
                }
                if (usbDevice == null) {
                    setSerialState(ConnectionEnum.TRYCONNECTERROR, String.format(serialService.getString(R.string.serialUsbDeviceFound), serialService.getString(R.string.serialErrorNoArduino)));
                    return false;
                }
                else {
                    setSerialState(ConnectionEnum.FOUND);
                }
            }
            else {
                setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoUsbManager);
                return false;
            }
        }
        else {
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNotIdle);
            return false;
        }
        return isFound();
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

    @Override
    protected boolean connect() {
        if (!isError()) {
            setSerialState(ConnectionEnum.TRYCONNECT);
        }

        if(!usbManager.hasPermission(usbDevice)){
            Log.e(TAG, serialService.getString(R.string.serialErrorNoUsbPermission));
            setSerialState(ConnectionEnum.TRYCONNECTERROR, serialService.getString(R.string.serialErrorNoUsbPermission));
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(serialService, 0, new Intent(
                    serialService.getString(R.string.USB_PERMISSION)), 0);
            usbManager.requestPermission(usbDevice, usbPermissionIntent);
            return false;
        }

        usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            setSerialState(ConnectionEnum.TRYCONNECTERROR,R.string.serialErrorUsbDeviceOpen);
            return false;
        }
        if(usbDevice.getInterfaceCount() < 2){
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorUsbNumInterface);
            return false;
        }
        usbInterface = usbDevice.getInterface(1);

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
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoUsbRxEndpoint);
            //usbConnection.close();
            return false;
        }
        if (usbEndpointTx == null) {
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorNoUsbTxEndpoint);
            //usbConnection.close();
            return false;
        }

        if (!usbConnection.claimInterface(usbInterface, true)) {
            setSerialState(ConnectionEnum.TRYCONNECTERROR, R.string.serialErrorUsbDeviceClaim);
            //usbConnection.close();
            return false;
        }
        setSerialState(ConnectionEnum.CONNECTED);
        return isConnected();
    }

    @Override
    protected boolean close() {
        //getSerialData().setSerialName(serialService.getString(R.string.serialDeviceNone));
        if(isRunning()){
            setSerialState(ConnectionEnum.IDLE);
        }
        else if(!isError()){
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorUnexpectedClose);
        }
        serialReceiveThread.interrupt();
        serialSendThread.interrupt();
        if (usbConnection != null) {
            if(usbInterface != null){
                if(!usbConnection.releaseInterface(usbInterface)){
                    serialService.sendToast( R.string.serialErrorUsbRelease);
                    setSerialState(ConnectionEnum.IDLE, R.string.serialErrorUsbRelease);
                }
                else{
                    Log.d(TAG,serialService.getString(R.string.serialUsbRelease));
                }
            }
            usbConnection.close();
            usbConnection = null;
        }
        usbManager = null;
        usbDevice = null;
        usbEndpointTx = null;
        usbEndpointRx = null;
        usbInterface = null;
        return isIdle();
    }


    @Override
    protected void send() throws IOException {
        byte[] buffer = null;
        if(getSerialData() != null) {
            buffer = getSerialData().serialTx.get();
        }
        if(buffer != null) {
            if((buffer.length > 0) && (usbConnection != null) && (usbEndpointTx != null)) {
                int len = usbConnection.bulkTransfer(usbEndpointTx, buffer, buffer.length, 0);
                //Log.d(TAG,"usb send: " + len);
            }
            else{
                setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
            }
        }
    }

    @Override
    protected int receive() throws IOException {

        final int BUFFER_LENGTH = RECEIVE_BUFFER_LENGTH;
        int acceptedFrame = 0;
        byte[] buffer = new byte[BUFFER_LENGTH];
        if ((buffer != null) && (usbConnection != null) && (usbEndpointRx != null) && (getSerialData() != null)) {
            int len = usbConnection.bulkTransfer(usbEndpointRx, buffer, buffer.length, 100);
            //int len = 6;
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (getSerialData().serialRx.append(buffer[i])) {
                        acceptedFrame++;
                    }
                }
                //Log.d(TAG, getSerialData().serialTx.byteArrayToHexString(buffer));
            }
        }
        else{
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
        }
        return acceptedFrame;
    }
}
