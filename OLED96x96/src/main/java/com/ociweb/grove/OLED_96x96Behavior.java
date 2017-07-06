package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.OLED.OLED_96x96.OLED_96x96_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.grove.I2CGroveTwig.*;

public class OLED_96x96Behavior implements StartupListener, TimeListener{
	private final OLED_96x96_Facade display;
	public OLED_96x96Behavior(FogRuntime rt){
		display = OLED_96x96.newFacade(rt.newCommandChannel(0,20000)); 
	}
	@Override
	public void timeEvent(long time, int iteration) {
		display.printCharSequence("hello world");
	}

	@Override
	public void startup() {
		display.setChip(0);
		display.init();
		display.clear();
		display.inverseOff();
		display.setVerticalMode();
	}

}
