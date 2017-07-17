package com.ociweb;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class IoTApp implements FogApp
{
	private static final Logger logger = LoggerFactory.getLogger(IoTApp.class);

	private final Port display_port = D5;
	@Override
	public void declareConnections(Hardware c) {
		c.connect(FourDigitDisplay, display_port);
		c.useI2C();
		c.setTriggerRate(1);
	}
	
	public static void main(String[] args){
		FogRuntime.run(new IoTApp());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new FourDigitDisplayBehavior(runtime, display_port));
	}
}
