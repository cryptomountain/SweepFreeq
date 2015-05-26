package com.cryptomountain.sweepfreeq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
  
  
import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
  
public class BluetoothConsole extends ActionBarActivity implements
		DataConnection {
	private static final String TAG = "bluetooth_console";

	Handler h;

	final int RECIEVE_MESSAGE = 1; // Status for Handler
	public Context context = null;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private Set<BluetoothDevice> pairedDevices;
	private StringBuilder sb = new StringBuilder();
	private String inBuff = new String(); // Input buffer

	private ConnectedThread mConnectedThread;

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// MAC-address of Bluetooth module (you must edit this line)
	//private static String address = "20:15:03:03:06:13";
	private ArrayList<DataUpdateListener> btListeners = new ArrayList<DataUpdateListener>();

	public BluetoothConsole(Context context){
		this.context = context;
		btAdapter = BluetoothAdapter.getDefaultAdapter(); 	// get Bluetooth adapter
		checkBTState();
		setupHandler();

	}
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupHandler();

		//btAdapter = BluetoothAdapter.getDefaultAdapter(); 	// get Bluetooth adapter
		//checkBTState();

	}

	public void addListener(DataUpdateListener listener) {
		this.btListeners.add(listener);
	}
	
	private void setupHandler(){
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				//Log.d(TAG,"...Incoming Message...");
				switch (msg.what) {
				case RECIEVE_MESSAGE: // if receive massage
					//Log.d(TAG,"...Incoming data to handle...");
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1); // create string from
					sb.append(strIncom); 								// append string
					
					int endOfLineIndex = sb.indexOf("\r\n"); 			// determine the end-of-line
					while (endOfLineIndex > 0) { 							// if end-of-line,
						inBuff = sb.substring(0, endOfLineIndex + 2 );
						if(!inBuff.equals("\r\n"))
							processBuffer();
						if(endOfLineIndex == sb.length())
							sb.delete(0,sb.length());
						else
							sb.delete(0, endOfLineIndex + 2); 						// and clear
						endOfLineIndex = sb.indexOf("\r\n");

					}

					break;
				}
			};
		};
	}
	
	private void processBuffer() {

		boolean doneSweeping = false;
		SweepData sd = new SweepData();
		String line = inBuff.trim();
		if(line.trim().equals("End"))
			doneSweeping = true;
		else{
			String[] ar = line.split(",");
			sd.setFreq(Float.valueOf(ar[0]) / 1000000);
			sd.setVswr(Float.valueOf(ar[1]) / 1000);
		}
		
		inBuff = "";
		
		// Notify listeners if the sweep is finished
		// and close the socket
		if (doneSweeping) {
			for (DataUpdateListener dul : btListeners) {
				dul.SweepDataUpdated(null);
			}
			

			try {
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		// Notify listeners that new data is available
		for (DataUpdateListener dul : btListeners) {
			dul.SweepDataUpdated(sd);
		}

	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "...onResume - try connect...");

		// Set up a pointer to the remote node using it's address.
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		String dev = SP.getString("pref_bluetoothDevice", "0");
		BluetoothDevice device = btAdapter.getRemoteDevice(dev);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: "
					+ e.getMessage() + ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error",
						"In onResume() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		try {
			btSocket.close();
		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket."
					+ e2.getMessage() + ".");
		}
	}

	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth not supported");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		//private final BufferedReader inStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			//inStream = in;
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[255]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer); // Get number of bytes and
														
					//String mssg = new String(buffer);
					//Log.e(TAG,"INCOMING: "+ mssg + "--");

					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer)
							.sendToTarget(); // Send to message queue Handler
					buffer = new byte[255];
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {
			Log.e(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				Log.e(TAG, "...Error data send: " + e.getMessage() + "...");
			}
		}
	}

	@Override
	public void setupConnection() {
		// Set up a pointer to the remote node using it's address.
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
		String dev = SP.getString("pref_bluetoothDevice", "0");
		BluetoothDevice device = btAdapter.getRemoteDevice(dev);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: "
					+ e.getMessage() + ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error",
						"In onResume() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
		}

	}

	@Override
	public void open() {
		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");
		
		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start(); 

	}

	@Override
	public void Send(String datastring) {
		mConnectedThread.write(datastring);

	}
}