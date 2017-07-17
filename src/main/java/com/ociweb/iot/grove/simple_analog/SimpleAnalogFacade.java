package com.ociweb.iot.grove.simple_analog;

import java.util.ArrayList;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceFacade;
import com.ociweb.iot.maker.Port;

public class SimpleAnalogFacade implements IODeviceFacade, AnalogListener{
	private FogCommandChannel ch;
	private Port p;
	private final ArrayList<SimpleAnalogListener> aListeners;
	private final ArrayList<RunningStdDevListener> rsListeners;
	private final ArrayList<MovingStdDevListener> msListeners;
	
	public SimpleAnalogFacade(FogCommandChannel ch, Port p, AnalogListenerable... ls){
		ch.ensurePinWriting();
		this.p  = p;
		this.ch = ch;
		aListeners = new ArrayList<SimpleAnalogListener>();
		rsListeners = new ArrayList<RunningStdDevListener>();
		msListeners = new ArrayList<MovingStdDevListener>();
		for (AnalogListenerable l: ls){
			if (l instanceof SimpleAnalogListener){
				aListeners.add((SimpleAnalogListener)l);
			}
			if (l instanceof RunningStdDevListener){
				rsListeners.add((RunningStdDevListener) l);
			}
			if (l instanceof MovingStdDevListener){
				msListeners.add((MovingStdDevListener) l);
			}
		}
	}
	
	public boolean setValue(int val){
		return ch.setValue(p, val);
	}
	
	public boolean setValueAndBlock(int val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		//aListener.analogEvent(port, time, durationMillis, average, value);
	}

}
