package com.cryptomountain.sweepfreeq;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDeviceConnection;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class UsbConsole extends ActionBarActivity implements DataConnection{
	
	protected static final String TAG = "USBCONSOLE";
	private Context context;
	private SerialInputOutputManager ioManager;
	private UsbManager manager;
	private UsbSerialDriver driver;
	private UsbSerialPort sPort;
	private UsbDeviceConnection connection = null;
	private ArrayList<DataUpdateListener> duListeners = new ArrayList<DataUpdateListener>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private String inBuff = new String();
	private int baudRate = 9600;
	
	
	private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
            UsbConsole.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UsbConsole.this.updateReceivedData(data);
                }
            });
        }
    };
	
    
	public UsbConsole(Context context){
		this.context = context;
	}
	
	public UsbConsole(Context context, UsbSerialPort port){
		this.context = context;
		sPort = port;
	}
	
	private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            ioManager = new SerialInputOutputManager(sPort, mListener);
            executor.submit(ioManager);
        }
    }
	
	 private void stopIoManager() {
		 if (ioManager != null) {
			 Log.i(TAG, "Stopping io manager ..");
			 ioManager.stop();
			 ioManager = null;
		 }
	 }
	
	protected void updateReceivedData(byte[] data) {
		
		try{
			inBuff+= new String(data, "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// Process the buffer when there is a full line of data
		// processBuffer() removes the completed line from the head of inBuff
		if(inBuff.contains("\r\n")){
			processBuffer();
		}
		
	}
	
	public void processBuffer(){

		boolean doneSweeping = false;
		SweepData sd = new SweepData();
		while(inBuff.contains("\r\n")){
			int idx = inBuff.indexOf("\r\n") + 1;
			String line=inBuff.substring(0, idx).trim();
			inBuff = inBuff.substring(idx+1);
			if(line.trim().equals("End")){
				doneSweeping = true;
			}else{
				// data sent for loading to database should be floats in MHz
				String[] ar = line.split(",");
				sd.setFreq(Float.valueOf(ar[0])/1000000);
				sd.setVswr(Float.valueOf(ar[1])/1000);
			}
		}
		
		// Notify listeners if the sweep is finished
		if(doneSweeping){
			for(DataUpdateListener dul: duListeners){
				dul.SweepDataUpdated(null);
			}
			return;
		}
		
		// Notifiy listeners that new data is available
		for(DataUpdateListener dul: duListeners){
			dul.SweepDataUpdated(sd);
		}
		
	}
	
	public void addListener(DataUpdateListener listener){
		this.duListeners.add(listener);
	}
	
	
	public void setupConnection(){
		UsbManager manager  = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
		if (availableDrivers.isEmpty()) {
		  return;
		}
		
		// Open a connection to the first available driver.
		driver = availableDrivers.get(0);
		connection = manager.openDevice(driver.getDevice());
		if (connection == null) {
		  // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
		  return;
		}
		
		List<UsbSerialPort>ports = driver.getPorts();
		sPort = ports.get(0);
		readPrefs();
	}
	

	public void open(){
		try{
			sPort.open(connection);
			sPort.setParameters(9600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
			startIoManager();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// TODO: should probably use ioManager for these
	public void Send(String message){
    	try{
    		message+="\r\n";
    	   	byte[] data = message.getBytes();
    	   	sPort.write(data, 3000);
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	return;
	}
	
	public void close(){
		try{
			sPort.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public UsbSerialPort getPort(){
		return sPort;
	}
	
	private void readPrefs(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		baudRate = Integer.valueOf(sharedPref.getString("pref_default_baud_rate", ""));
		System.out.print("baudRate = " + baudRate);
	}

}
