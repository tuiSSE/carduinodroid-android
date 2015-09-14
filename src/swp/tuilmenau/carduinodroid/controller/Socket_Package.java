package swp.tuilmenau.carduinodroid.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import swp.tuilmenau.carduinodroid.CarDuinoDroidAppActivity;
import swp.tuilmenau.carduinodroid.model.LOG;

/**
 * This class is used to send information to the PC
 * 
 * @author Robin
 */
public class Socket_Package implements Runnable {

	private static final String ACKNOWLEDGE = "ACK";
	BufferedWriter packagewriter;
	ServerSocket socket_package;
	Socket client;
	Activity activity;
	Controller_Android controller_Android;
	BufferedReader packagereader;
	private Network network;
	private boolean newPreviewSizes = true;
	public Handler arduinoHandler;
	private int[] arduinoData;
	private long arduinoLastDataTime;

	/**
	 * The constructor
	 * 
	 * @param n_controller_Android
	 */
	public Socket_Package(Activity parentActivity, Controller_Android n_controller_Android, Network nnetwork) {
		activity = parentActivity;
		controller_Android = n_controller_Android;
		network = nnetwork;
		arduinoData = new int[ArduinoCommunicator.BUFFERSIZE];
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicator.DATA_RECEIVED_READY_INTENT);
        filter.addAction(ArduinoCommunicator.DATA_RECEIVED_EXTRA);
        
        activity.getBaseContext().registerReceiver(mReceiver, filter);
	}

	/**
	 * Sends the information package to the socket
	 * 
	 * @param infopackage
	 *            The information
	 * @return true if successful
	 */
	public boolean sendpackage(String infopackage) {
		if (client.isConnected()) {
			try {
				packagewriter.write(infopackage);
				packagewriter.newLine();
				packagewriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			return true;
		}

		else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ServerSocket socket_package = null;
//		Looper.prepare();
//		
//		arduinoHandler = new Handler(){
//			@Override
//			public void handleMessage(Message msg) {
//				arduinoData = (byte[]) msg.obj;
//			}			
//		};
		
		while (true) {
			client = null;
			packagewriter = null;
			int i = 0;
			int teiler = 0;
			boolean fail = true;
			try {
				if (socket_package == null)
					socket_package = new ServerSocket(12346);
				client = socket_package.accept();
				packagewriter = new BufferedWriter(new OutputStreamWriter(
						client.getOutputStream()));
				packagereader = new BufferedReader(new InputStreamReader(
						client.getInputStream()));

			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}


			while (!client.isClosed() && fail) {

				if(client != null && newPreviewSizes){
					sendCameraSizes();
					newPreviewSizes = false;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				i++;
				if (i > 15) {
					sendpackage(ACKNOWLEDGE);
					new Thread(new Runnable() {

						public void run() {
							long currenttime = System.currentTimeMillis();
							boolean getMessage = false;
							while (currenttime + 10000 > System.currentTimeMillis() && client.isConnected() && !getMessage) {
								try {
									if (packagereader.ready()) {
										String msg = packagereader.readLine();
										if (msg.equals(ACKNOWLEDGE)) {
											getMessage = true;
										}
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if (!getMessage) {
								network.close();
							}
						}
					}).start();
					i = 0;
				}
				teiler++;
				if(teiler > 9){
				String message = controller_Android.packData();
				sendpackage(message);
				teiler=0;}
				
				sendVibration();
				sendUltraSoundData();
			}
		} 

	}
	public void sendCameraSizes() {
		if(network.controller.cam == null){
			return;
		}
		String sizes = network.controller.cam.getSupportedSize();
		try {
			packagewriter.write("2;" + sizes);
			packagewriter.newLine();
			packagewriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			network.controller.log.write(LOG.WARNING, "Error by writing onto Packagesocket");
		}
	}
	
	
	public void sendVibration() {
		if (client.isConnected()) {
			String vibration = controller_Android.gps.Vibration();
			try {
				packagewriter.write("3;" + vibration);
				packagewriter.newLine();
				packagewriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	public void sendUltraSoundData() {
		if (client.isConnected()) {
//			if ((ultrasoundFrontData < 100) && (ultrasoundBackData < 100)){
//				ultrasoundFrontData++;
//				ultrasoundBackData++;
//			}else{
//				ultrasoundFrontData = 0;
//				ultrasoundBackData = 0;
//			}
//			if (controller_Android.arduinoCommunicator.dataIsReady()){
//				byte[] data = controller_Android.arduinoCommunicator.getData();
//				Log.d("Car Data", "Data sent: " + new String(data));
//				ultrasoundFrontData = data[5];
//				ultrasoundBackData = data[6];
//			}
//			Log.d("Car Data", "Data sent: " + new String(arduinoData));
			float carCurrent = arduinoData[0] / 10;
			int carBatteryAbsolute = arduinoData[1] * 10;
			int carBatteryProcetual = arduinoData[2];
			float carVoltage = arduinoData[3];
			int carTemperature = arduinoData[4];
//			if (carTemperature > 40){
//				carTemperature = 66;
//			}
			int ultrasoundFrontData = arduinoData[5];
			int ultrasoundBackData = arduinoData[6];
			
			if (System.currentTimeMillis() > arduinoLastDataTime + 10000 ){
				
				try {
					packagewriter.write("5;");
					packagewriter.newLine();
					packagewriter.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}else{			
				try {
					packagewriter.write("4;" 	+ carCurrent + ";"
												+ carBatteryAbsolute + ";"
												+ carBatteryProcetual + ";"
												+ carVoltage + ";"
												+ carTemperature + ";"
												+ ultrasoundFrontData + ";" + ultrasoundBackData);
					packagewriter.newLine();
					packagewriter.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
	}

	public void close() {
		try {
			if (client != null)
				client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	activity.unregisterReceiver(mReceiver);
	}

	public void newPreviewSizes() {
		newPreviewSizes  = true;
	}
	
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            arduinoLastDataTime = System.currentTimeMillis();

            if (ArduinoCommunicator.DATA_RECEIVED_READY_INTENT.equals(action)) {
            	byte[] data = intent.getByteArrayExtra(ArduinoCommunicator.DATA_RECEIVED_EXTRA);
            	for(int i = 0; i < data.length ; i++){
            		arduinoData[i] = data[i] & 0xFF; // byte - signed to integer - unsigned
            	}                
            }
        }
    };
}
