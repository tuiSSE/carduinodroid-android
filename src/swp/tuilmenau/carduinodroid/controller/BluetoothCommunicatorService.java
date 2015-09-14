package swp.tuilmenau.carduinodroid.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Currency;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

public class BluetoothCommunicatorService extends Service{
	// UUID fuer Kommunikation mit Seriellen Modulen
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "Bluetooth";
 
    // Variablen
    private Handler toastHandler;
    private SenderThread mSenderThread;
    private boolean mIsRunning = false;
    private BluetoothAdapter adapter = null;
    private BluetoothSocket socket = null;
    private OutputStream stream_out = null;
    private InputStream stream_in = null;
    private boolean is_connected = false;
    BluetoothDevice remote_device =null;
    private static String mac_adresse; // MAC Adresse des Bluetooth Adapters
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        toastHandler = new Handler();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicator.SEND_DATA_INTENT);
        registerReceiver(mReceiver, filter);
        
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(mIsRunning == true){
    		return Service.START_REDELIVER_INTENT;
    	}
    	
    	mIsRunning = true;
    	
        // Verbindung mit Bluetooth-Adapter herstellen
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            toast("Gerät unterstützt kein Bluetooth");
            Log.d(TAG, "Gerät unterstützt kein Bluetooth");
        }else{
            Log.d(TAG, "Bluetooth-Adapter ist bereit");
        }
        if (!adapter.isEnabled()){
        	adapter.enable();
        }
        
//        while(!connect()){
//        	try {
//				wait(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
        
        new Thread(new Runnable(){
        	public void run(){
        		while(!is_connected){
        			connect();
        		}		
        	}
        }, "connectBluetoothThread").start();

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
    	stop();
        super.onDestroy();
    }
    
    
    
    public boolean connect() {
		mac_adresse = uuid.toString();
		Log.d(TAG, "Verbinde mit " + mac_adresse);
		
		if(adapter == null){
			return false;
		}
		if(!adapter.isEnabled()){
			adapter.enable();
//			return false;
		}
		
//		Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
//
//		ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
//
//		
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		if (devices != null) {
		    for (BluetoothDevice device : devices) {
		        if (device.getName().toString().contains("HC-06")) {
		            remote_device = device;
		            break;
		        }
		    }
		}
		if(remote_device == null){
			toast("No paired Ardunio found!");
			return false;
		}
		
//		remote_device = adapter.getRemoteDevice(mac_adresse);

		// Socket erstellen
		try {
			socket = remote_device.createInsecureRfcommSocketToServiceRecord(uuid);
			Log.d(TAG, "Socket erstellt");
		} catch (Exception e) {
			Log.e(TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
		}

		adapter.cancelDiscovery();

		// Socket verbinden
		try {
			socket.connect();
			Log.d(TAG, "Socket verbunden");
			is_connected = true;
		} catch (IOException e) {
			is_connected = false;
			Log.e(TAG, "Socket kann nicht verbinden: " + e.toString());
		}

		// Socket beenden, falls nicht verbunden werden konnte
		if (!is_connected) {
			try {
				socket.close();
			} catch (Exception e) {
				Log.e(TAG,"Socket kann nicht beendet werden: " + e.toString());
			}
		}

		// Outputstream erstellen:
		try {
			stream_out = socket.getOutputStream();
			Log.d(TAG, "OutputStream erstellt");
		} catch (IOException e) {
			Log.e(TAG, "OutputStream Fehler: " + e.toString());
			is_connected = false;
		}

		// Inputstream erstellen
		try {
			stream_in = socket.getInputStream();
			Log.d(TAG, "InputStream erstellt");
		} catch (IOException e) {
			Log.e(TAG, "InputStream Fehler: " + e.toString());
			is_connected = false;
		}

		if (is_connected) {
			toast("Verbunden mit " + remote_device.getName());
			startSenderThread();
			startReceiverThread();
			return true;
		} else {
			toast("Verbindungsfehler mit " + mac_adresse);
			return false;
		}
		
	}
    
	public void disconnect() {
		if (is_connected && stream_out != null) {
			is_connected = false;
			Log.d(TAG, "Trennen: Beende Verbindung");
			try {
				stream_out.flush();
				socket.close();
			} catch (IOException e) {
				Log.e(TAG,"Fehler beim beenden des Streams und schliessen des Sockets: "+ e.toString());
			}
			is_connected = false;
		} else
			Log.d(TAG, "Trennen: Keine Verbindung zum beenden");
	}
    
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ArduinoCommunicator.SEND_DATA_INTENT.equals(action)) {
                final byte[] dataToSend = intent.getByteArrayExtra(ArduinoCommunicator.DATA_EXTRA);
                if (dataToSend == null) {
                    return;
                }

                mSenderThread.mHandler.obtainMessage(10, dataToSend).sendToTarget();
            } else if (!is_connected) {
                mSenderThread.mHandler.sendEmptyMessage(11);
                stopSelf();
            }
        }
    };
    
    private void startReceiverThread() {
        new Thread("arduino_Bluetooth_receiver") {
            @Override
			public void run() {
                byte[] inBuffer = new byte[ArduinoCommunicator.BUFFERSIZE];
                int length = 0;
                while(is_connected) {
                	Arrays.fill( inBuffer, (byte) 0 );
                	try {
            			if (stream_in.available() > 0) {
            				length = stream_in.read(inBuffer);
            				Log.d(TAG,"Anzahl empfangender Bytes: " + String.valueOf(length));
            			} else{
            				try {
								Thread.currentThread().sleep(100);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//            				Toast.makeText(this, "Nichts empfangen", Toast.LENGTH_LONG).show();
            			}
            		} catch (Exception e) {
            			Log.e(TAG, "Fehler beim Empfangen: " + e.toString());
            		}
                    if (length > 0) {
                        Intent intent = new Intent(ArduinoCommunicator.DATA_RECEIVED_INTENT);
                        byte[] buffer = new byte[length];
                        System.arraycopy(inBuffer, 0, buffer, 0, length);
                        intent.putExtra(ArduinoCommunicator.DATA_EXTRA, buffer);
                        sendBroadcast(intent);
                    }
                 Log.d(TAG, "Bluetoothreceiver thread stopped.");
                }
            }
        }.start();
    }
	
    private void startSenderThread() {
        mSenderThread = new SenderThread("arduino_Bluetooth_sender");
        mSenderThread.start();
    }
	
	private class SenderThread extends Thread {
        public Handler mHandler;

        public SenderThread(String string) {
            super(string);
        }

        @Override
		public void run() {

            Looper.prepare();

            mHandler = new Handler() {
                @Override
				public void handleMessage(Message msg) {
                    if (msg.what == 10) {
                        final byte[] dataToSend = (byte[]) msg.obj;
                        if (is_connected) {
                			Log.d(TAG, "Sende Nachricht: " + msg);
                			try {
                				stream_out.write(dataToSend);
                			} catch (IOException e) {
                				Log.e(TAG,"Bluetooth: Exception beim Senden: " + e.toString());
                			}
                        }
                        Intent sendIntent = new Intent(ArduinoCommunicator.DATA_SENT_INTERNAL_INTENT);
                        sendIntent.putExtra(ArduinoCommunicator.DATA_EXTRA, dataToSend);
                        sendBroadcast(sendIntent);
                    } else if (msg.what == 11 || !is_connected) {
                        Looper.myLooper().quit();
                    }
                }
            };

            Looper.loop();
//            if (DEBUG) Log.i(TAG, "sender thread stopped");
        }
    }
	
    public void stop(){
    	unregisterReceiver(mReceiver);
//    	adapter.disable();
    	disconnect();
    	stopSelf();
    }
    
    public void toast(final String msg){  
    	toastHandler.post(new Runnable() {
    	    public void run() {
    	        Toast toast = Toast.makeText(BluetoothCommunicatorService.this, msg, Toast.LENGTH_SHORT);
    	        toast.show();
    	    }
    	 });
    }
}
