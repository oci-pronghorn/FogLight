package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import static com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64_Constants.*;

import com.ociweb.iot.grove.OLED.OLED_128x64.Grove_OLED_128x64;
import com.ociweb.iot.maker.*;


public class OLED128x64 implements FogApp
{



	@Override
	public void declareConnections(Hardware c) {
		c.useI2C();
		c.setTriggerRate(125);
		c.enableTelemetry(true);
		//TODO: give warning message if trigger rate was not set and time listener is used
	}

	public static void main (String[] args){
		FogRuntime.run(new OLED128x64());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel(0,20000);
		GameOfLife game = new GameOfLife(ch);
		runtime.addListener(game);
	}



}
