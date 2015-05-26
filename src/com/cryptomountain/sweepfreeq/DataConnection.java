package com.cryptomountain.sweepfreeq;

import android.content.Context;

public interface DataConnection {
	public Context context = null;
	
	
	public void setupConnection();
	public void open();
	public void Send(String datastring);
	public void addListener(DataUpdateListener listener);
	

}
