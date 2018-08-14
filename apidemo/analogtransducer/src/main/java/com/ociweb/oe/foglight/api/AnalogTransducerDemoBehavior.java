package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class AnalogTransducerDemoBehavior implements  AnalogListener{ //SimpleAnalogListener, MovingAverageListener,
	private final int bucketSize = 15;
	private final FogRuntime runtime;

	//private final SimpleAnalogTransducer sensor;
	public AnalogTransducerDemoBehavior(FogRuntime runtime, Port p){
		//sensor = new SimpleAnalogTransducer(p, this); //no command channel needed because reading
		//sensor.registerListener(this,bucketSize); //this is also the implementation of MovingAverageListener
	//	rt.registerListener(sensor);
		this.runtime = runtime;
	}
	/*
	@Override
	public void movingAverage(double ma) {
		System.out.println("Moving Average: " + ma);
	}

	@Override
	public void simpleAnalogEvent(Port port, long time, long durationMillis, int value) {
		System.out.println("Value:" +value);
	}
*/
	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		System.out.println("Analog Event: "+ value);
		runtime.shutdownRuntime();
	}
	//@Override
	public void timeEvent(long arg0, int arg1) {
		System.out.println(arg0);
	}


}
