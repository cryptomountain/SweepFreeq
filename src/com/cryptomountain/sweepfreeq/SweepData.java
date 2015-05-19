package com.cryptomountain.sweepfreeq;

public class SweepData {
	private long id;
	private float freq;
	private float vswr;
	
	public SweepData(){}
	
	public SweepData(long id, float freq, float vswr){
		this.id = id;
		this.freq = freq;
		this.vswr = vswr;
	}
	
	public long getId(){
		return id;
	}
	
	public void setId(long _id){
		id = _id;
	}
	
	public float getFreq(){
		return freq;
	}
	
	public void setFreq(float _freq){
		freq=_freq;
	}
	
	public float getVswr(){
		return vswr;
	}
	
	public void setVswr(float _vswr){
		vswr=_vswr;
	}
	
	
	@Override
	public String toString(){
		String me=String.valueOf(freq)+","+String.valueOf(vswr);
		return me;
	}

}
