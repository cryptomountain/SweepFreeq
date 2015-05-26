/**
 * @author Joe Culbreth <joe@cryptomountain.com>
 * 2015-05-19
 * Copyright © 2015
 */

package com.cryptomountain.sweepfreeq;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Sweeper extends AsyncTask<Void,SweepData,Void> implements DataUpdateListener{
	public final static String USB_CONSOLE = "usbConsole";
	
	private int steps = 100;
	private float startFreq = 1.2F;
	private float stopFreq = 30.0F;
	public Context context;
	List<DataUpdateListener> listeners = new ArrayList<DataUpdateListener>();
	private SQLiteDatabase db;
	private SweepDatabaseHelper dbh;
	ArrayList<SweepData> inComing=new ArrayList<SweepData>();
	private String consoleType = null;
		
	public Sweeper(Context context){
		this.context = context;
	}
	
	public Sweeper(Context context, int steps){
		this.context=context;
		this.steps=steps;
	}
	
	public Sweeper(Context context, int steps, float startFreq, float stopFreq){
		this.context = context;
		this.steps=steps;
		this.startFreq=startFreq;
		this.stopFreq=stopFreq;
	}
	
	public void addListener(DataUpdateListener listener){
		this.listeners.add(listener);
	}
	
	public void setConsoleType(String type){
		consoleType = type;
	}
	
	/**
	 * Use this for loading test data when you don't care if it blocks
	 */
	public void loadBogusData(){
		try{
			Random generator = new Random();
			float freqstep = (stopFreq - startFreq)/steps;
			
			dbh = new SweepDatabaseHelper(context);
			db = dbh.getWritableDatabase();
			
			// empty the table
			db.delete(SweepDatabaseHelper.TABLE_SWEEPDATA, null, null);
			// start writing the data
			for(int i=0;i<steps;i++){
				ContentValues values = new ContentValues();
				SweepData sdata=new SweepData( (long)i, startFreq+(i*freqstep), (float)generator.nextFloat()*10 );
				values.put(SweepDatabaseHelper.COLUMN_ID, (long)i);
				values.put(SweepDatabaseHelper.COLUMN_FREQ, startFreq+(i*freqstep));
				values.put(SweepDatabaseHelper.COLUMN_VSWR, sdata.getVswr());
				db.insert(SweepDatabaseHelper.TABLE_SWEEPDATA, null, values);
				// Notify all the listeners
				for(DataUpdateListener dul: listeners){
					dul.SweepDataUpdated(sdata);
				}
			}
			dbh.close();
			for(DataUpdateListener dul: listeners){
				dul.SweepDataUpdated(null);
			}
		}catch(Exception e){
			// do Nothing at the moment
		}
	}
	
	/**
	 * Use this to load data buffered in inComing
	 */
	public void loadData(){
		try{
			dbh = new SweepDatabaseHelper(context);
			db = dbh.getWritableDatabase();
			
			// empty the table
			db.delete(SweepDatabaseHelper.TABLE_SWEEPDATA, null, null);
			// start writing the data
			for(int i=0;i<inComing.size();i++){
				ContentValues values = new ContentValues();
				SweepData sdata=new SweepData( (long)i, inComing.get(i).getFreq(), inComing.get(i).getVswr() );
				values.put(SweepDatabaseHelper.COLUMN_ID, sdata.getId());
				values.put(SweepDatabaseHelper.COLUMN_FREQ, sdata.getFreq());
				values.put(SweepDatabaseHelper.COLUMN_VSWR, sdata.getVswr());
				db.insert(SweepDatabaseHelper.TABLE_SWEEPDATA, null, values);
			}
			dbh.close();
			return;
		}catch(Exception e){
			e.printStackTrace();
			return;
		}finally{
			if(db != null && dbh != null && db.isOpen()){
				db.close();
			}	
		}
	}	
	/**
	 * These implement the AsyncTask, and will spin off in a new thread
	 * for bogus data
	 */
	@Override
	protected Void doInBackground(Void... voids) {
		try{
			Random generator = new Random();
			float freqstep = (stopFreq - startFreq)/steps;
			
			dbh = new SweepDatabaseHelper(context);
			db = dbh.getWritableDatabase();
			
			// empty the table
			db.delete(SweepDatabaseHelper.TABLE_SWEEPDATA, null, null);
			// start writing the data
			for(int i=0;i<steps;i++){
				ContentValues values = new ContentValues();
				SweepData sdata=new SweepData( (long)i, startFreq+(i*freqstep), (float)generator.nextFloat()*10 );
				values.put(SweepDatabaseHelper.COLUMN_ID, (long)i);
				values.put(SweepDatabaseHelper.COLUMN_FREQ, startFreq+(i*freqstep));
				values.put(SweepDatabaseHelper.COLUMN_VSWR, sdata.getVswr());
				db.insert(SweepDatabaseHelper.TABLE_SWEEPDATA, null, values);
				publishProgress(new SweepData[]{sdata});
			}
			dbh.close();
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(db != null && dbh != null && db.isOpen()){
				db.close();
			}
			
		}
		
	}
	
	@Override
	protected void onProgressUpdate(SweepData...datas){
		for(DataUpdateListener du: listeners){
			du.SweepDataUpdated(datas[0]);
		}
		return;
	}
	
	@Override
	protected void onPostExecute(Void v) {
		for(DataUpdateListener du: listeners){
			du.SweepDataUpdated(null);
		}
		return;
    }
	
	/**
	 * End of AsyncTask methods
	 */
	
	public void doSweep(){
		// build command
		// send command
		// get new data
		// load to database
		// display the graph

		DataConnection connection = new UsbConsole(context);
		connection.addListener((DataUpdateListener) this);
		connection.setupConnection();
		try{
			connection.open();
			String command = String.valueOf((long)(startFreq * 1000000)) + "A" + String.valueOf((long)(stopFreq * 1000000)) + "B" + String.valueOf(steps) + "NS";
			connection.Send(command);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	// Listeners for connection data
	@Override
	public void SweepDataUpdated(SweepData data) {
		if(data != null){
			inComing.add(data);
			for(DataUpdateListener du: listeners){
				du.SweepDataUpdated(data);
			}
		}else{
			loadData();
			for(DataUpdateListener du: listeners){
				du.SweepDataUpdated(null);
			}
		}

	}

}
