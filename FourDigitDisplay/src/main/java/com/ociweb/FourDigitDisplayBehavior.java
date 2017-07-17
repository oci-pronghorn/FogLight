package com.ociweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ociweb.gl.api.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.four_digit_display.FourDigitDisplayCommand.*;

import static com.ociweb.iot.maker.FogCommandChannel.*;
public class FourDigitDisplayBehavior implements TimeListener,StartupListener {

	private static final Logger logger = LoggerFactory.getLogger(FourDigitDisplayBehavior.class);

	private final FogCommandChannel ch;
	private final Port p;

	
	public FourDigitDisplayBehavior(FogRuntime r, Port p){
		this.ch = r.newCommandChannel(PIN_WRITER);
		this.p = p;
	}
	@Override
	public void timeEvent(long time, int iteration) {
		
		ch.setValue(p,iteration % 1000);

	}
	@Override
	public void startup() {
		ch.setValue(p, INIT);
		ch.setValue(p, SET_BRIGHTNESS + 7);
		ch.setValue(p, DISPLAY_ON);
	}
}
