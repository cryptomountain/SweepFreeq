package com.cryptomountain.sweepfreeq;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SweepDataDAO {

  // Database fields
  private SQLiteDatabase database;
  private SweepDatabaseHelper dbHelper;
  private String[] allColumns = { SweepDatabaseHelper.COLUMN_ID,
      SweepDatabaseHelper.COLUMN_FREQ,
      SweepDatabaseHelper.COLUMN_VSWR};

  public SweepDataDAO(Context context) {
    dbHelper = new SweepDatabaseHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  // Don't use this yet!
  public SweepData createSweepData(float freq, float vswr) {
    ContentValues values = new ContentValues();
    values.put(SweepDatabaseHelper.COLUMN_FREQ, freq);
    values.put(SweepDatabaseHelper.COLUMN_VSWR, vswr);
    long insertId = database.insert(SweepDatabaseHelper.TABLE_SWEEPDATA, null,
        values);
    Cursor cursor = database.query(SweepDatabaseHelper.TABLE_SWEEPDATA,
        allColumns, SweepDatabaseHelper.COLUMN_ID + " = " + insertId, null,
        null, null, null);
    cursor.moveToFirst();
    SweepData newSweepData = cursorToSweepData(cursor);
    cursor.close();
    return newSweepData;
  }
  
  public void clearData(){
	database.delete(SweepDatabaseHelper.TABLE_SWEEPDATA, null, null); 
  }
  
  public void insertData(SweepData row){
	  ContentValues values = new ContentValues();
	  values.put(SweepDatabaseHelper.COLUMN_ID, row.getId());
	  values.put(SweepDatabaseHelper.COLUMN_FREQ, row.getFreq());
	  values.put(SweepDatabaseHelper.COLUMN_VSWR, row.getVswr());
	  	  
	  database.insert(SweepDatabaseHelper.TABLE_SWEEPDATA, null, values);
  }
  

  public void deleteSweepData(SweepData data) {
    long id = data.getId();
    System.out.println("SweepData deleted with id: " + id);
    database.delete(SweepDatabaseHelper.TABLE_SWEEPDATA, SweepDatabaseHelper.COLUMN_ID
        + " = " + id, null);
  }

  public List<SweepData> getAllSweepData() {
    List<SweepData> rows = new ArrayList<SweepData>();

    Cursor cursor = database.query(SweepDatabaseHelper.TABLE_SWEEPDATA,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
    	SweepData data = cursorToSweepData(cursor);
    	rows.add(data);
    	cursor.moveToNext();
    }
    // make sure to close the cursor
    cursor.close();
    return rows;
  }

  private SweepData cursorToSweepData(Cursor cursor) {
	SweepData data = new SweepData();
    data.setId(cursor.getLong(0));
    data.setFreq(cursor.getFloat(1));
    data.setVswr(cursor.getFloat(2));
    return data;
  }
  
}