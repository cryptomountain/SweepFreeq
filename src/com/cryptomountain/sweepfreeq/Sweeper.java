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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Sweeper extends AsyncTask<Void,SweepData,Void>{
	private int steps = 100;
	private float startFreq = 1.2F;
	private float stopFreq = 30.0F;
	public Context context;
	List<DataUpdateListener> listeners = new ArrayList<DataUpdateListener>();
	private SQLiteDatabase db;
	private SweepDatabaseHelper dbh;
	
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
	
	/**
	 * Use this for loading when you don't care if it blocks
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
				for(DataUpdateListener du: listeners){
					du.SweepDataUpdated(sdata);
				}
			}
			dbh.close();
			for(DataUpdateListener du: listeners){
				du.SweepDataUpdated(null);
			}
		}catch(Exception e){
			// do Nothing at the moment
		}
	}
	
	
	/**
	 * These implement the AsyncTask, and will spin off in a new thread
	 * 
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
	

}
