/**
 * @author Joe Culbreth <joe@cryptomountain.com>
 * 05-14-2015
 * Copyright 2015
 * 
 * Reads data from sqlite db and charts it
 * 
 * -Instantiate from an Activity, passing in the context
 * -Get a LineChart View that you want to graph to
 * -Invoke drawGraph with the LineChart object
 * 
 * 
 * 
 */
package com.cryptomountain.sweepfreeq;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import android.graphics.Color;
import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class ChartMaker{

	private LineDataSet data;
	private ArrayList<String> xVals = new ArrayList<String>();
	public Context context;
	
	
	public ChartMaker(Context context){
		this.context=context;
	}
	
	
	public void setChartData(LineDataSet linedataset){
		data=linedataset;
	}
	
	public LineDataSet getData(){
		return data;
	}
	
	public ArrayList<String> getXVals(){
		return xVals;
	}
	
	public void readSweepData(){
		SweepDataDAO db = new SweepDataDAO(context);
		ArrayList<Entry> datapoints = new ArrayList<Entry>();
		db.open();
		List<SweepData> sdata = db.getAllSweepData();
		for(int i =0; i<sdata.size(); i++){
			Entry e= new Entry(sdata.get(i).getVswr(),(int)sdata.get(i).getId());
			datapoints.add(e);
			xVals.add(String.valueOf(sdata.get(i).getFreq()));
		}
		data = new LineDataSet(datapoints,"VSWR");
		db.close();
	}

	
	public void drawGraph(LineChart chart){
		
		//LineChart chart = (LineChart)v.findViewById(R.id.chart);
		// TODO: Throw exceptions instead of returning nothing
		if(chart == null){
			return;
		}
		
		readSweepData();
		int background = Color.WHITE;
		int foreground = Color.BLUE; 
		
		chart.setBackgroundColor(background);
		chart.setGridBackgroundColor(background);
		
		
		chart.setDescription("VSWR");
		chart.setDescriptionTextSize(10);
		chart.setDescriptionColor(foreground);
		chart.setDrawBorders(true);
		
		
		if(data == null || data.getEntryCount() < 1 )
			return;
		
		data.setAxisDependency(AxisDependency.LEFT);
		data.setColor(foreground);
		data.setCircleColor(foreground);
		data.setCircleSize(3);
		data.setValueTextColor(foreground);
		
		ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
		dataSets.add(data);
		
		LineData data = new LineData(xVals,dataSets);
		chart.setData(data);
		chart.invalidate();
		
	}
	
	
	////////////////////////
	// Utilities
	////////////////////////
	
	
	
	public static ArrayList<Double> generateDoubleVals(int num){
		ArrayList<Double> list= new ArrayList<Double>(num);
		Random generator = new Random();
		for(int i=0;i<num;i++)
			list.add(generator.nextDouble());
		return list;
	}
	
	public static ArrayList<Integer> generateIntegerVals(int num){
		ArrayList<Integer> list = new ArrayList<Integer>(num);
		Random generator = new Random();
		for(int i=0; i<num; i++)
			list.add(generator.nextInt(300));
		return list;
	}
	
	public static LineDataSet generateLineDataSet(int num){
		Random generator = new Random();
		ArrayList<Entry> points = new ArrayList<Entry>(num);
		for(int i=0; i<num; i++){
			points.add(new Entry(generator.nextFloat(),i));
		}
		LineDataSet lds= new LineDataSet(points,"VSWR");
		
		return lds;
	}
	
	public static ArrayList<String> generateXVals(int num){
		ArrayList<String> x = new ArrayList<String>();
		for(int i=0;i<num;i++)
			x.add(String.valueOf(i));
		return x;
	}
	
	public static ArrayList<String> createXfromLineDataSet(LineDataSet lds){
		ArrayList<String> x=new ArrayList<String>();
		for(int i=0; i<lds.getEntryCount();i++)
			x.add(String.valueOf(lds.getEntryForXIndex(i).getVal()));
		return x;
		
	}

}
