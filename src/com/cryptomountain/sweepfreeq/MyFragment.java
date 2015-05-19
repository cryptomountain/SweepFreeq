package com.cryptomountain.sweepfreeq;

import com.cryptomountain.sweepfreeq.R;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class MyFragment extends Fragment{


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.my_fragment, container, false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    if (savedInstanceState != null) {
			EditText startfreq = (EditText)getActivity().findViewById(R.id.editTextStartFreq);
			EditText stopfreq = (EditText)getActivity().findViewById(R.id.editTextStopFreq);
			EditText steps = (EditText)getActivity().findViewById(R.id.editTextSteps);
			TextView freq = (TextView)getActivity().findViewById(R.id.dispFreq);
			TextView vswr = (TextView)getActivity().findViewById(R.id.dispVswr);
			
			startfreq.setText(savedInstanceState.getCharSequence("startfreq"));
			stopfreq.setText(savedInstanceState.getCharSequence("stopfreq"));
			steps.setText(savedInstanceState.getCharSequence("steps"));
			freq.setText(savedInstanceState.getCharSequence("freq"));
			vswr.setText(savedInstanceState.getCharSequence("vswr"));
	    }
	}
	

	@Override
	public void onSaveInstanceState( Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		EditText startfreq = (EditText)getActivity().findViewById(R.id.editTextStartFreq);
		EditText stopfreq = (EditText)getActivity().findViewById(R.id.editTextStopFreq);
		EditText steps = (EditText)getActivity().findViewById(R.id.editTextSteps);
		TextView freq = (TextView)getActivity().findViewById(R.id.dispFreq);
		TextView vswr = (TextView)getActivity().findViewById(R.id.dispVswr);
		
		savedInstanceState.putCharSequence("StartFreq",startfreq.getText());
		savedInstanceState.putCharSequence("stopfreq", stopfreq.getText());
		savedInstanceState.putCharSequence("steps", steps.getText());
		savedInstanceState.putCharSequence("freq", freq.getText());
		savedInstanceState.putCharSequence("vswr", vswr.getText());
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}

}
