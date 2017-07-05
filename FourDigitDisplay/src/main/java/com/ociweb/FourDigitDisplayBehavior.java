package com.ociweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class FourDigitDisplayBehavior implements TimeListener {

	private static final Logger logger = LoggerFactory.getLogger(FourDigitDisplayBehavior.class);

	private final FogCommandChannel ch;
	private final Port p;
	
	public FourDigitDisplayBehavior(FogRuntime r, Port p){
		this.ch = r.newCommandChannel();
		this.p = p;
	}
	@Override
	public void timeEvent(long time, int iteration) {
		ch.setValue(p,iteration % 1000);
	}

}
