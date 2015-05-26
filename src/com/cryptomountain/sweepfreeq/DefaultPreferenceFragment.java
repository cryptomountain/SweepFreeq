/**
 * 
 */
package com.cryptomountain.sweepfreeq;


import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceCategory;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * @author root
 *
 */
public class DefaultPreferenceFragment extends PreferenceFragment {
	private BluetoothAdapter myBluetoothAdapter;
	private Set<BluetoothDevice> pairedDevices;
	private ListView myListView;
	private ArrayAdapter<String> BTArrayAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.default_preferences);
        
         //addPreferencesFromResource(R.layout.serial_settings_fragment);
    }

}
