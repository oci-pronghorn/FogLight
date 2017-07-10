package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import static com.ociweb.iot.grove.oled.OLEDTwig.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.grove.oled.OLED_96x96_Facade;

public class OLED_96x96Behavior implements StartupListener, TimeListener{
	private final OLED_96x96_Facade display;
	public OLED_96x96Behavior(FogRuntime rt){
		display = OLED_96x96.newFacade(rt.newCommandChannel(0,20000)); 
		
	}
	@Override
	public void timeEvent(long time, int iteration) {
		if ((iteration + 1) %12 != 0){
			display.setTextRowCol(iteration % 12, 0);
			display.printCharSequence("hello world");
		}
		else {
			display.cleanClear();
		}
		
	}

	@Override
	public void startup() {
		display.init();
		display.cleanClear();
		display.setVerticalMode();
		
	}

}
