/**
 * @author Joe Culbreth <joe@cryptomountain.com>
 * 2015-05-19
 * Copyright © 2015
 */

package com.cryptomountain.sweepfreeq;

import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cryptomountain.sweepfreeq.R;
import com.github.mikephil.charting.charts.LineChart;


public class MainActivity extends ActionBarActivity implements DataUpdateListener {
	private boolean sweepRunning = false;
	ParamFragment myfrag = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.default_preferences, false);
		setContentView(R.layout.activity_main);
		ChartMaker charter=new ChartMaker(this);
		LineChart chart=(LineChart)findViewById(R.id.chart);
		charter.drawGraph(chart);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		EditText et1=(EditText)findViewById(R.id.editTextStartFreq);
		EditText et2=(EditText)findViewById(R.id.editTextStopFreq);
		EditText et3=(EditText)findViewById(R.id.editTextSteps);
		et1.setText(prefs.getString("pref_startFreq", "1.25"));
		et2.setText(prefs.getString("pref_stopFreq", "28.1"));
		et3.setText(prefs.getString("pref_Steps", "110"));
		
		RadioGroup serialTypeG = (RadioGroup) findViewById(R.id.radioSerialCommType);
		String defaultType = prefs.getString("pref_default_device_type", "bluetooth");
		if(defaultType.equals("usb"))
			serialTypeG.check(R.id.radioUsb);
		else
			serialTypeG.check(R.id.radioBluetooth);
			
		if(savedInstanceState == null){
			myfrag = (ParamFragment)getSupportFragmentManager().findFragmentById(R.id.paramFragment);
		}
		if (savedInstanceState != null)   {
			myfrag = (ParamFragment) getSupportFragmentManager().getFragment(savedInstanceState, "paramFragment");			
		}
			 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState( Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		myfrag = (ParamFragment)getSupportFragmentManager().findFragmentById(R.id.paramFragment);
		getSupportFragmentManager().putFragment(savedInstanceState, "paramFragment", myfrag );
		
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  myfrag = (ParamFragment) getSupportFragmentManager().getFragment(savedInstanceState, "myFragment");

	}
	
	@Override
	public void onPause(){
		super.onPause();
		
	}

	@Override
	public void onResume(){
		super.onResume();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent settingIntent =  new Intent(this, SettingsActivity.class);
			startActivity(settingIntent);
			return true;
		}
		if(id == R.id.action_reset){
			popMessage(R.string.ma_reset_title, R.string.ma_reset_message);
			return true;
		}
		if(id == R.id.action_exit){
			finish();
			System.exit(0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public void SweepDataUpdated(SweepData data){
		if(data == null){
			sweepRunning = false;
			drawChart();
			return;
		}
		TextView freq=(TextView)findViewById(R.id.dispFreq);
		TextView vswr=(TextView)findViewById(R.id.dispVswr);
		freq.setText(String.format("%1$,.3f MHz", data.getFreq()));
		vswr.setText(String.format("%1$,.2f : 1",data.getVswr()));
		
	}
	

	public void onSweepClick(View v){
		//popMessage(v);
		if(sweepRunning){
			popMessage(R.string.ma_sweep_running_title, R.string.ma_sweep_running_message);
			return;
		}
		sweepRunning = true;
		// Get Values for Sweeping
		EditText et1=(EditText)findViewById(R.id.editTextStartFreq);
		EditText et2=(EditText)findViewById(R.id.editTextStopFreq);
		EditText et3=(EditText)findViewById(R.id.editTextSteps);
		RadioGroup serialTypeG = (RadioGroup) findViewById(R.id.radioSerialCommType);
		int selectedOption = serialTypeG.getCheckedRadioButtonId();
		RadioButton serialTypeB = (RadioButton)findViewById(selectedOption);
		String serialType = new String();
		if( serialTypeB.getText().toString().equals("USB"))
			serialType = Sweeper.USB_CONSOLE;
		else
			serialType = Sweeper.BLUETOOTH_CONSOLE;
		
		
		final Sweeper sweeper = new Sweeper(this, et3.getText().toString().isEmpty()?30:Integer.valueOf(et3.getText().toString()),
				et1.getText().toString().isEmpty()?1.75F:Float.valueOf(et1.getText().toString()),
				et2.getText().toString().isEmpty()?30.0F:Float.valueOf(et2.getText().toString()));
		sweeper.addListener(this);
		sweeper.setConsoleType(serialType);
		//sweeper.execute((Void[])null);
		//sweeper.executeOnExecutor(Executors.newSingleThreadExecutor(),(Void[])null);
		sweeper.doSweep();
	}

	
	public void drawChart(){
		ChartMaker charter=new ChartMaker(this);
		LineChart chart=(LineChart)findViewById(R.id.chart);
		charter.drawGraph(chart);
		float f = charter.getFreqMin();
		float min = charter.getVswrMin();
		
		TextView tvFreq = (TextView)findViewById(R.id.dispFreq);
		TextView tvVswr = (TextView)findViewById(R.id.dispVswr);
		tvFreq.setText( String.format("%1$,.3f MHz",f) );
		tvVswr.setText( String.format("%1$,.2f : 1",min) );
	}
		
	
	public void onUsbClick(View v){
		Intent intent = new Intent(this, SerialConsoleActivity.class);
		startActivity(intent);
	}
	
	public void onBluetoothClick(View v){
		Intent intent = new Intent(this, BluetoothActivity.class);
		startActivity(intent);
	}
	
	////////////////////////////////
	// Useful Message Boxes
	/////////////////////////////////
	
	public void popMessage(View v){
		popMessage("Pop-up", "Something activated me!");
	}
	
	public void popMessage(int title_id,int message_id){
		String title= getResources().getString(title_id);
		String message= getResources().getString(message_id);
		popMessage(title,message);
	}
	
	public void popMessage(String title,String message){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
			      //alertDialog.cancel();
			   }
			});
		// Set the Icon for the Dialog
		//alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}
}
