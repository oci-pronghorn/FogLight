package com.ociweb;

import static com.ociweb.iot.maker.Port.D5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
public class IoTApp implements FogApp
{
	private static final Logger logger = LoggerFactory.getLogger(IoTApp.class);

	private final Port display_port = D5;
	@Override
	public void declareConnections(Hardware c) {
		
	}
	
	public static void main(String[] args){
		FogRuntime.run(new IoTApp());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new FourDigitDisplayBehavior(runtime, display_port));
	}
}
