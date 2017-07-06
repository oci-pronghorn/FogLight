package com.ociweb.grove;


import static com.ociweb.iot.grove.I2CGroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;

public class OLED96x96 implements FogApp
{
	private static final Logger logger = LoggerFactory.getLogger(OLED96x96.class);
	@Override
	public void declareConnections(Hardware c) {
		c.setTriggerRate(1000);
		c.useI2C();

	}
	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.addListener(new OLED_96x96Behavior(runtime));
	}
	public static void main(String[] args){
		FogRuntime.run(new OLED96x96());
	}
}
