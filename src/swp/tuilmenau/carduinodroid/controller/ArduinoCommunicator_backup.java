package swp.tuilmenau.carduinodroid.controller;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class ArduinoCommunicator_backup {
	
	public Activity activity;
	
    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;
    
    private final static String TAG = "ArduinoCommunicatorActivity";
    private final static boolean DEBUG = true;
    
    final static String DATA_RECEIVED_READY_INTENT = "swp.carduinodroid.intent.action.DATA_RECEIVED_READY";
    final static String DATA_RECEIVED_EXTRA = "swp.carduinodroid.intent.extra.DATA_RECEIVED_EXTRA";
    
    final static String DATA_RECEIVED_INTENT = "swp.carduinodroid.intent.action.DATA_RECEIVED";
    final static String SEND_DATA_INTENT = "swp.carduinodroid.intent.action.SEND_DATA";
    final static String DATA_SENT_INTERNAL_INTENT = "swp.carduinodroid.internal.intent.action.DATA_SENT";
    final static String DATA_EXTRA = "swp.carduinodroid.intent.extra.DATA";
    
    private UsbDevice usbDevice;
    
    public ArduinoCommunicator_backup(Activity parentActivity){
    	activity = parentActivity;
    	
        IntentFilter filter = new IntentFilter();
        filter.addAction(DATA_RECEIVED_INTENT);
        filter.addAction(DATA_SENT_INTERNAL_INTENT);
        
        activity.getBaseContext().registerReceiver(mReceiver, filter);
    	
        findDevice();
        resetReceivedData();
    }
    
    private void findDevice() { 
        UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        if (DEBUG) Log.d(TAG, "length: " + usbDeviceList.size());
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();

            // Print device information. If you think your device should be able
            // to communicate with this app, add it to accepted products below.
            if (DEBUG) Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
            if (DEBUG) Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
            if (DEBUG) Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            if (DEBUG) Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            if (DEBUG) Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            if (DEBUG) Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            if (DEBUG) Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            if (DEBUG) Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                if (DEBUG) Log.i(TAG, "Arduino device found!");

                switch (tempUsbDevice.getProductId()) {
                case ARDUINO_UNO_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Uno " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Uno " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Mega 2560 " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Mega 2560 " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Mega 2560 R3 " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Mega 2560 R3 " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_UNO_R3_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Uno R3 " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Uno R3 " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Mega 2560 ADK R3 " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Mega 2560 ADK R3 " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
//                    Toast.makeText(activity.getBaseContext(), "Arduino Mega 2560 ADK " + "found", Toast.LENGTH_SHORT).show();
                	toast("Arduino Mega 2560 ADK " + "found");
                    usbDevice = tempUsbDevice;
                    break;
                }
            }
        }

        if (usbDevice == null) {
            if (DEBUG) Log.i(TAG, "No device found!");
//            Toast.makeText(activity.getBaseContext(), "No Device found", Toast.LENGTH_LONG).show();
            toast("No Device found");
        } else {
            if (DEBUG) Log.i(TAG, "Device found!");
//            Intent startIntent = new Intent(activity.getApplicationContext(), ArduinoCommunicatorService.class);
//            PendingIntent pendingIntent = PendingIntent.getService(activity.getApplicationContext(), 0, startIntent, 0);
//            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }
    
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        private void handleTransferedData(Intent intent, boolean receiving) {
        	
        	if(receiving){
        		handleReceivedData(intent);
        	}     	
            
            final byte[] newTransferedData = intent.getByteArrayExtra(DATA_EXTRA);
            if (DEBUG) Log.i(TAG, "data: " + newTransferedData.length + " \"" + new String(newTransferedData) + "\"");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive() " + action);

            if (DATA_RECEIVED_INTENT.equals(action)) {
                handleTransferedData(intent, true);
            } else if (DATA_SENT_INTERNAL_INTENT.equals(action)) {
                handleTransferedData(intent, false);
            }
        }
    };

    byte[] msgBuffer = new byte[4096];   
    int msgLength;
    int totalLength;
	Calendar c = Calendar.getInstance(); 
	long lastReceiveTime ;
    
    private void resetReceivedData(){
    	Arrays.fill(msgBuffer, (byte) 0);
    	totalLength = 0;
    	msgLength = 0;
    	lastReceiveTime = System.currentTimeMillis();
    }
    
    private void handleReceivedData(Intent intent){
    	if(System.currentTimeMillis() - lastReceiveTime > 100){
    		resetReceivedData();
    	}
    	
    	byte[] inBuffer;
    	inBuffer = intent.getByteArrayExtra(DATA_EXTRA);
    	if(inBuffer[0] < 0){
    		return;
    	}
    	int length = inBuffer.length;
    	if (totalLength == 0){
    		int protocolHeader = inBuffer[0];
        	int protocol = (protocolHeader & 0xC0) >> 6;
        	msgLength = (protocolHeader & 0x0F);
        	if(msgLength >= totalLength + length -1){
        		System.arraycopy(inBuffer, 1, msgBuffer, 0, length-1);
        	}
        	totalLength += length-1;
    	}else{
        	if(msgLength >= totalLength + length){
        		System.arraycopy(inBuffer, 0, msgBuffer, totalLength, length);
    			totalLength += length;
        	}     	
    	}
  	    
    	if(msgLength == totalLength){
            Intent dataReadyIntent = new Intent(DATA_RECEIVED_READY_INTENT);
            dataReadyIntent.putExtra(DATA_RECEIVED_EXTRA, msgBuffer);
            activity.sendBroadcast(dataReadyIntent);
            Log.d(TAG, new String(msgBuffer));
//            Toast.makeText(activity.getBaseContext(), new String(msgBuffer), Toast.LENGTH_LONG).show();
            toast(new String(msgBuffer));
    		resetReceivedData();
    	}
    	lastReceiveTime = System.currentTimeMillis();
    }
    
    public void send(byte[] data){    	
    	if(usbDevice != null){
       	 	Intent intent = new Intent(SEND_DATA_INTENT);
       	 	//byte[] buffer = {(byte) 0x42, 0x65, 0x66};
       	 	intent.putExtra(DATA_EXTRA, data);
            activity.sendBroadcast(intent);
    	}
    }
    
    public boolean hasDevice(){
    	if(usbDevice != null)
    		return true;
    	else return false;
    }
    
    public void stop(){
    	activity.unregisterReceiver(mReceiver);
    	activity.stopService(new Intent(activity.getApplicationContext(),USBCommunicatorService.class));
    }
    
    public void toast(final String msg){  	
		activity.runOnUiThread(new Runnable(){
		     public void run() {
		          // UI code goes here
		    	 Toast.makeText(activity.getBaseContext(), msg, Toast.LENGTH_SHORT).show();
		     }
		});
    }
}

