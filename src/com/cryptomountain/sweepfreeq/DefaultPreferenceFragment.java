/**
 * 
 */
package com.cryptomountain.sweepfreeq;


import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
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
        
        // Dynamically build Bluetooth devices for selection
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = myBluetoothAdapter.getBondedDevices();
 
        // Generate the lists
        ListPreference listPreferenceCategory = (ListPreference) findPreference("pref_bluetoothDevice");
        if (listPreferenceCategory != null) {
            
            CharSequence entries[] = new String[pairedDevices.size()];
            CharSequence entryValues[] = new String[pairedDevices.size()];
            int i=0;
            for(BluetoothDevice device : pairedDevices){
                entries[i] = device.getName();
                entryValues[i] = device.getAddress();
                i++;
            }
            listPreferenceCategory.setEntries(entries);
            listPreferenceCategory.setEntryValues(entryValues);
        }

        //addPreferencesFromResource(R.layout.serial_settings_fragment);
    }

}
