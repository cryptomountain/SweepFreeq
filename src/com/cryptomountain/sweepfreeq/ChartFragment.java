/**
 * @author Joe Culbreth <joe@cryptomountain.com>
 * 2015-05-19
 * Copyright 2015
 */

package com.cryptomountain.sweepfreeq;

import com.cryptomountain.sweepfreeq.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ChartFragment extends Fragment{

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View chartView = inflater.inflate(R.layout.chart_fragment, container, false);
		return chartView;
	}
	
}
