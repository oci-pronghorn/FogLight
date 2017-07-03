package com.ociweb.grove;


import static com.ociweb.iot.grove.I2CGroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;

public class OLED96x96 implements FogApp
{
	@Override
	public void declareConnections(Hardware c) {
		c.useI2C();
		c.setTriggerRate(500);

	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		
		final FogCommandChannel ch = runtime.newCommandChannel(0,20000);
		OLED_96x96_Facade display = OLED_96x96.newFacade(ch); 
	
		runtime.addStartupListener(()->{
			display.init();
			display.setTextRowCol(0, 0);
			display.setContrast(255);
		});
		
		runtime.addTimeListener((time,interation) -> {
			display.printCharSequence("Hello world");
		});
		

	}
	public static void main(String[] args){
		FogRuntime.run(new OLED96x96());
	}
}
