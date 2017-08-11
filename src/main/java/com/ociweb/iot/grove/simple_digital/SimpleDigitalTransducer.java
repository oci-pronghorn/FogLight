package com.ociweb.iot.grove.simple_digital;

import java.util.ArrayList;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;



/**
 * 
 * @author Ray Lo
 *
 */
public class SimpleDigitalTransducer implements IODeviceTransducer, DigitalListener {
	private final FogCommandChannel ch;
	private final Port p;

	private final ArrayList<SimpleDigitalListener> simpleDigListeners;

	public SimpleDigitalTransducer(FogCommandChannel ch, Port p, SimpleDigitalListener... dl){
		ch.ensurePinWriting();
		this.ch = ch;
		this.p = p;
		simpleDigListeners = new ArrayList<SimpleDigitalListener>();

		int i = dl.length;
		while (--i >= 0 ){
			simpleDigListeners.add(dl[i]);
		}


	}
	/**
	 * Puts onto the {@link FogCommandChannel} a high or low value for the simple digital device.
	 * @param val the boolean value to be sent, true is high and false is low.
	 * @return true if command was put onto the {@link FogCommandChannel}
	 */
	public boolean setValue(boolean val){
		return ch.setValue(p, val);
	}

	/**
	 * Puts onto the {@link FogCommandChannel} a high or low value for the simple digital device and blocks the channel.
	 * @param val the boolean value to be sent, true is high and false is low.	
	 * @param durationMillis the number of milliseconds to block the channel for. Other channels will be uanffected.
	 * @return true if command was put onto the {@link FogCommandChannel}
	 */
	public boolean setValueAndBlock(boolean val, long durationMillis){
		return ch.setValueAndBlock(p, val, durationMillis);
	}
	
	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		int i = simpleDigListeners.size();
		while (--i >= 0){
			simpleDigListeners.get(i).simpleDigitalEvent(port, time, durationMillis, value);
		}

	}
}
