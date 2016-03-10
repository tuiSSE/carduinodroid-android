package tuisse.carduinodroid_android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import tuisse.carduinodroid_android.data.ConnectionEnum;
import tuisse.carduinodroid_android.data.SerialType;

import static tuisse.carduinodroid_android.data.Utils.byteArrayToHexString;
/**
 * <h1>Serial USB</h1>
 * This class holds the implementation of the serial usb connection
 *
 * @author Till Max Schwikal
 * @version 1.0
 * @since 04.01.2016
 */
public class SerialUsb extends SerialConnection {
    private final String TAG = "CarduinoSerialUsb";
    private UsbManager usbManager = null;
    private UsbDevice usbDevice = null;
    private UsbDeviceConnection usbConnection = null;
    private UsbEndpoint usbEndpointRx = null;
    private UsbEndpoint usbEndpointTx = null;
    private UsbInterface usbInterface = null;

    public SerialUsb(SerialService s) {
        super(s);
        if(getData() != null) {
            getData().setSerialType(SerialType.USB);
        }
        else{
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
        }
    }

    /**
     * find checks if the right usb device partner (an arduino) is connected
     * @return true if the connected device is a known arduino, else otherwise
     */
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
                    //usbDevice = null;
                    for (UsbDevice tempUsbDevice : deviceList.values()) {
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

                        if (tempUsbDevice.getVendorId() == Constants.USB_VENDOR_ID.ARDUINO) {
                            switch (tempUsbDevice.getProductId()) {
                                case Constants.USB_PRODUCT_ID.ARDUINO_UNO:
                                    getData().setSerialName(serialService.getString(R.string.arduino_uno));
                                    usbDevice = tempUsbDevice;
                                    break;
                                case Constants.USB_PRODUCT_ID.ARDUINO_UNO_R3:
                                    getData().setSerialName(serialService.getString(R.string.arduino_uno_r3));
                                    usbDevice = tempUsbDevice;
                                    break;
                                case Constants.USB_PRODUCT_ID.ARDUINO_MEGA_2560:
                                    getData().setSerialName(serialService.getString(R.string.arduino_mega));
                                    usbDevice = tempUsbDevice;
                                    break;
                                case Constants.USB_PRODUCT_ID.ARDUINO_MEGA_2560_R3:
                                    getData().setSerialName(serialService.getString(R.string.arduino_mega_r3));
                                    usbDevice = tempUsbDevice;
                                    break;
                                case Constants.USB_PRODUCT_ID.ARDUINO_MEGA_2560_ADK:
                                    getData().setSerialName(serialService.getString(R.string.arduino_mega_adk));
                                    usbDevice = tempUsbDevice;
                                    break;
                                case Constants.USB_PRODUCT_ID.ARDUINO_MEGA_2560_ADK_R3:
                                    getData().setSerialName(serialService.getString(R.string.arduino_mega_adk_r3));
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


    /**
     * helper function to generate the proper usb line encoding
     * @param baudRate is the selected baud rate
     * @return proper line encoding byte array
     */
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

    /**
     * connects with the usb device, checks the permission and opens the usb device and
     * setups the usb endpoints
     * @return true on a successful connect
     */
    @Override
    protected boolean connect() {
        if (!isError()) {
            setSerialState(ConnectionEnum.TRYCONNECT);
        }

        if(!usbManager.hasPermission(usbDevice)){
            Log.e(TAG, serialService.getString(R.string.serialErrorNoUsbPermission));
            setSerialState(ConnectionEnum.TRYCONNECTERROR, serialService.getString(R.string.serialErrorNoUsbPermission));
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(serialService, 0, new Intent(
                    Constants.PERMISSION.USB), 0);
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

    /**
     * closees the usb connection normally to free the resources and get to a suitable state
     * @note some android usb drivers are unable to close the connection
     * @return true if succesfully closed
     */
    @Override
    protected boolean close() {
        //getSerialData().setSerialName(serialService.getString(R.string.serialDeviceNone));
        Log.d(TAG,"Closing serial connection");
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

    /**
     * sends a serial frame over usb
     * @throws IOException
     */
    @Override
    protected void send() throws IOException {
        byte[] buffer = null;
        if(getData() != null) {
            buffer = getDataHandler().serialFrameAssembleTx();
        }
        if(buffer != null) {
            if((buffer.length > 0) && (usbConnection != null) && (usbEndpointTx != null)) {
                int len = usbConnection.bulkTransfer(usbEndpointTx, buffer, buffer.length, 0);
                if(Constants.LOG.SERIAL_SENDER) {
                    Log.d(TAG,"usb send" + len + ": " + byteArrayToHexString(getDataHandler().serialFrameAssembleTx()));
                }
            }
            else{
                setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
            }
        }
    }

    /**
     * receives incoming bytes over usb
     * @return true if a valid frame was received
     * @throws IOException
     */
    @Override
    protected int receive() throws IOException {

        final int BUFFER_LENGTH = RECEIVE_BUFFER_LENGTH;
        int acceptedFrame = 0;
        byte[] buffer = new byte[BUFFER_LENGTH];
        if ((usbConnection != null) && (usbEndpointRx != null) && (getData() != null)) {
            int len = usbConnection.bulkTransfer(usbEndpointRx, buffer, buffer.length, 100);
            //int len = 6;
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (getDataHandler().serialFrameAppendRx(buffer[i])) {
                        acceptedFrame++;
                    }
                }
                if(Constants.LOG.SERIAL_RECEIVER){
                    Log.d(TAG, byteArrayToHexString(buffer));
                }
            }
        }
        else{
            setSerialState(ConnectionEnum.ERROR, R.string.serialErrorNoDataPointer);
        }
        return acceptedFrame;
    }
}
