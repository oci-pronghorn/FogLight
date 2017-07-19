package com.ociweb.iot.grove.simple_analog;

import java.util.ArrayList;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.util.ma.MAvgRollerLong;
import static com.ociweb.pronghorn.util.ma.MAvgRollerLong.*;

public class SimpleAnalogFacade implements IODeviceFacade, AnalogListener{
	private FogCommandChannel ch;
	private Port p;

	private final ArrayList<SimpleAnalogListener> aListeners;
	private final ArrayList<RunningStdDevListener> rsListeners;
	private final ArrayList<MovingStdDevListener> msListeners;
	private final ArrayList<MovingAverageListener> maListeners;

	private final ArrayList<MAvgRollerLong> maRollers;

	/**
	 * Listeners can only be added upon construction.
	 * @param ch
	 * @param p
	 * @param ls var args of listeners specific to this SimpleAnalog device's AnalogEvent.
	 */
	public SimpleAnalogFacade(FogCommandChannel ch, Port p, SimpleAnalogListener... ls){
		
		this.p  = p;
		this.ch = ch;

		aListeners = new ArrayList<SimpleAnalogListener>();
		rsListeners = new ArrayList<RunningStdDevListener>();
		msListeners = new ArrayList<MovingStdDevListener>();
		maListeners = new ArrayList<MovingAverageListener>();

		maRollers = new ArrayList<MAvgRollerLong>();

		for (SimpleAnalogListener l: ls){
			aListeners.add(l);
		}
		if (ch != null){
			ch.ensurePinWriting();
		}
	}
	
	public SimpleAnalogFacade(Port p, SimpleAnalogListener... ls){
		this.p  = p;
		this.ch = null;

		aListeners = new ArrayList<SimpleAnalogListener>();
		rsListeners = new ArrayList<RunningStdDevListener>();
		msListeners = new ArrayList<MovingStdDevListener>();
		maListeners = new ArrayList<MovingAverageListener>();

		maRollers = new ArrayList<MAvgRollerLong>();

		for (SimpleAnalogListener l: ls){
			aListeners.add(l);
			System.out.println("Added: " + l.toString());
		}
	}
	
	public boolean setValue(int val){
		return ch.setValue(p, val);
	}

	public boolean setValueAndBlock(int val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}

	public SimpleAnalogFacade registerListener(RunningStatsListener l){
		if (l instanceof RunningStdDevListener){
			rsListeners.add((RunningStdDevListener) l);
		}
		
		if (l instanceof MovingAverageListener){

		}
		return this;
	}

	public SimpleAnalogFacade registerListener(MovingStatsListener l, int bucketSize){
		if ( l instanceof MovingAverageListener){
			maListeners.add((MovingAverageListener) l);
			maRollers.add(new MAvgRollerLong(bucketSize));
			
		}
		if (l instanceof MovingStdDevListener){
			msListeners.add((MovingStdDevListener) l);
		}
		return this;
	}
	
	

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		//aListener.analogEvent(port, time, durationMillis, average, value);
		System.out.println("HERE");
		if (port.equals(p)){
			System.out.println("THERE");
			for (SimpleAnalogListener sal: aListeners){
				sal.simpleAnalogEvent(port, time, durationMillis, value);
			}
			for (int i = 0; i < maListeners.size(); i ++){
				MAvgRollerLong.roll(maRollers.get(i), value);
				maListeners.get(i).movingAverage(mean(maRollers.get(i)));
			}
			
		
			
			//TODO: HANDLE THE OTHER 3 STATS LISTENER
		}
	}

}
