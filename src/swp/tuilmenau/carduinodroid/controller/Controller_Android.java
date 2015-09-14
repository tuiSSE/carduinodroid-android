package swp.tuilmenau.carduinodroid.controller;


import android.app.Activity;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;
import swp.tuilmenau.carduinodroid.model.LOG;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

/**
 * Wraps all other classes into this class to be used by the activity.
 * 
 * @author Paul Thorwirth
 * @author Lars
 * @version 1.0
 */
public class Controller_Android 
{
	public LOG log;
	
	public Cam cam;
	public Connection connection;
	public GPS gps;
	public Network network;
	public Record_Sound record_sound;
	public Sound sound;
	
	public Activity activity;
//	private static UsbController sUsbController;
	public static ArduinoCommunicator arduinoCommunicator;
	private static final int VID = 0x2341;
	private static final int PID = 0x0043;//I believe it is 0x0000 for the Arduino Megas
	public boolean sent;
	public boolean sentfree;
	public boolean lightStatus;
	public boolean autoStopStatus = true;
	/**
	 * Calls the Constructor of all other sub-classes.
	 * 
	 * @param activity the current Activity
	 */
	public Controller_Android(Activity parentActivity) 
	{
		log = new LOG();
		//test
		activity = parentActivity;
		cam = new Cam(this, activity);
		connection = new Connection(activity, log);
		gps = new GPS(this, activity, log);
		record_sound = new Record_Sound(log);
		sound = new Sound(activity, log);
		sent=false;

		final Controller_Android temp = this;
		
		Thread t = new Thread(new Runnable()
		{
			public void run() 
			{
				@SuppressWarnings("unused")
				Network network = new Network(activity, temp);
			}
		});
		t.setName("Network");
		t.start();
		
		arduinoCommunicator = new ArduinoCommunicator(activity);
	}

	/**
	 * Collects all Info and prepares the Info-Package to be sent to the PC-Client.
	 *
	 * @return A {@link String} containing the compressed Data.
	 */
	public String packData() 
	{
		String data;
		data = "1;";
		if (connection.getMobileAvailable()) data = data + 1 + ";"; 
										else data = data + 0 + ";";
		if (connection.getWLANAvailable())   data = data + 1 + ";"; 
										else data = data + 0 + ";";	
		if (connection.getMobile())			 data = data + 1 + ";"; 
										else data = data + 0 + ";";
		if (connection.getWLAN()) 			 data = data + 1 + ";"; 
										else data = data + 0 + ";";	
		
		data = data + gps.getGPS() + ";";

		return data;
	}

	/**
	 * Used to decode a packed command String received from the PC. 
	 * After the decode the commands are executed.
	 *
	 * @param data A String containing the compressed Data as follows:
	 * <ol>
	 * 	<li>Control Signals with settings</li>
	 * 	<li>Camera Settings</li>
	 *	<ol>
	 *		<li>Front or Back Camera</li>
	 * 		<li>Camera Resolution</li>
	 * 		<li>Camera Light</li>
	 * 		<li>Quality</>
	 * 	</ol>
	 * 	<li>Sound Signals</li>
	 * 	<ol>
	 * 		<li>Play a Sound by Android phone</li>
	 * 		<li>Start or Stop a Record</li>
	 * 	</ol>
	 * </ol>
	 */
	public void receiveData(String data)
	{
		final String TRUE_STRING = "true";
		
		//temporary variable
		final boolean oldVersion = false; 
		
		boolean front, right;
		String[] tokens = data.split(";",-1);


		switch (Integer.parseInt(tokens[0]))
		{
			case 1: // Everything for control signals
			{		
				
				if (tokens.length > 5){
		
					sent=false; 
					
					if(arduinoCommunicator == null){
						arduinoCommunicator = new ArduinoCommunicator(activity);
						log.write(LOG.INFO, "New Arduino Communicator");
					}
					
//					if(sUsbController == null){
//						sUsbController = new UsbController(ACTIVITY, mConnectionHandler, VID, PID, log, this);
//						log.write(LOG.INFO, "New USBController");}
					/*else{
						sUsbController.stop();
						sUsbController = new UsbController(ACTIVITY, mConnectionHandler, VID, PID, log, this);
						log.write(LOG.INFO, "USBController.stop und New USB Controller");
					}*/ //Just to have the power for closing the USB-Controller an enumerate again
					
					// tokens[0] - data, camera,...
					// tokens[1] - speed
					// tokens[2] - driving direction (true:forward)
					// tokens[3] - steer angle
					// tokens[4] - steer direction (true:right)
					// tokens[5] - autoStop
	
					front = (tokens[2].equals(TRUE_STRING));
					right = (tokens[4].equals(TRUE_STRING));
					autoStopStatus = (tokens[5].equals(TRUE_STRING));
//					autoStopStatus = true;
					
					byte[] commando = new byte[4];
					
					if (oldVersion)
					{
					commando[0] = Byte.parseByte(tokens[1]);
					commando[1] = (byte)(front?1:0);
					commando[2] = Byte.parseByte(tokens[3]);
					commando[3] = (byte)(right?1:0);
					}
					else
					{
						int version = 1;
						int length = 3;
						int speedValue = Integer.parseInt(tokens[1]);
						int steerSteps = Integer.parseInt(tokens[3]);
						int speedDirection = front?1:0;
						int steerDirection = right?1:0;
						int light = lightStatus?1:0;
						int autoStop = autoStopStatus?1:0;
						
						commando[0] = (byte)(0x00 | length | (version << 6));
						commando[1] = (byte)(0x00 | (speedDirection << 3) | (speedValue << 4));
						commando[2] = (byte)(0x00 | (steerDirection << 3) | (steerSteps << 4));
						commando[3] = (byte)(0x00 | (light << 6) | (autoStop << 5) );
//						commando[3] = (byte)(0x00 | (light << 6) | (0 << 5));
						
						StringBuilder sb = new StringBuilder();
					    for (byte b : commando) {
					        sb.append(String.format("%02X ", b));
					    }
					    log.write(LOG.INFO, (sb.toString()));
					}
					
					if(arduinoCommunicator != null){
						arduinoCommunicator.send(commando);
					}
//					if(sUsbController != null){
//						sUsbController.send(commando);
//					}

				}
	
			} break;
			
			case 2: // Everything for camera settings
			{
				switch (Integer.parseInt(tokens[1]))
				{
					case 1:
					{
						if(cam == null){
							break;
						}
						cam.switchCam(Integer.parseInt(tokens[2])); //Anpassen mit Robin
						log.write(LOG.INFO, "Switched Camera");
					} break;
					case 2:
					{
						Log.wtf("cam", "reschange" + tokens[2]);
						cam.changeRes(Integer.parseInt(tokens[2])); //Anpassen mit Robin
					} break;
					
					case 3:
					{
						if (Integer.parseInt(tokens[2]) == 1){
							cam.enableFlash();
							lightStatus = true;
						}
						if (Integer.parseInt(tokens[2]) == 0) {
							cam.disableFlash();
							lightStatus = false;
						}
					} break;
					
					case 4:
					{
						cam.setQuality(Integer.parseInt(tokens[2]));
					} break;
			
					default: log.write(LOG.WARNING, "Unknown camera command from PC"); break;
				}
			} break;
			
			case 3: // Everything with sounds
			{
				switch (Integer.parseInt(tokens[1]))
				{
					case 1: 
					{	
						sound.horn();
					} break;
					
					case 2:
					{
						if (Integer.parseInt(tokens[2]) == 0) 
						{
							record_sound.stop();
						}
						else 
						{
							record_sound.start();
						}
					} break;
				
					default: log.write(LOG.WARNING, "Unknown Sound command from PC"); break;
				}
			} break;

			default: log.write(LOG.WARNING, "unknown command from PC"); break;
		}
	}

	/**
	 * Sets the Network
	 * 
	 * @param network the Network to set.
	 */
	public void setNetwork(Network network) 
	{
		this.network = network;
	}
	
	public void SetSent(boolean Sent){
		sent = Sent;
	}
	
	public boolean GetSent(){
		return sent;
	}
	
	public void stop(){
		arduinoCommunicator.stop();
//		network.close();
	}
	
//	private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
//		public void onUsbStopped() {
//			log.write(LOG.INFO, "USB Connection gestoppt");
//		}
//		
//		public void onErrorLooperRunningAlready() {
//			log.write(LOG.INFO, "Looper l�uft schon");
//		}
//		
//		public void onDeviceNotFound() {
//			if(sUsbController != null){
//				sUsbController.stop();
//				sUsbController = null;
//			}
//			log.write(LOG.INFO, "Device nicht gefunden");
//		}
//	};
	
}