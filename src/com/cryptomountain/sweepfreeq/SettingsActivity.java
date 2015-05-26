package com.cryptomountain.sweepfreeq;

import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.view.View;

public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DefaultPreferenceFragment())
                .commit();
       

    }
    

}
