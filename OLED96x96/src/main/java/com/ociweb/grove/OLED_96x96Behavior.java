package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import static com.ociweb.iot.grove.oled.OLEDTwig.*;

import com.ociweb.iot.maker.FogRuntime;

import com.ociweb.iot.grove.oled.OLED_96x96_Facade;
import static com.ociweb.grove.OCI_Logo.*;
import static com.ociweb.grove.Grumpy.*;
import static com.ociweb.grove.PiLogo.*;
import static com.ociweb.grove.DexterLogo.*;


public class OLED_96x96Behavior implements StartupListener, TimeListener{
	private final OLED_96x96_Facade display;
	int[][] image;
	public OLED_96x96Behavior(FogRuntime rt){
		display = OLED_96x96.newFacade(rt.newCommandChannel(0,20000)); 

	}
	@Override
	public void timeEvent(long time, int iteration) {
		int remainder = iteration % 4;
		
		switch (remainder){
		case 0:
			display.displayImage(OCI_LOGO);
			break;
		case 1:
			display.displayImage(GRUMPY);
			break;
		case 2:
			display.displayImage(PI_LOGO);
			break;
		case 3:
			display.displayImage(DEX_LOGO);
			break;
		}
		
	}

	@Override
	public void startup() {
		display.init();
		display.cleanClear();
		display.setHorizontalMode();
		display.displayImage(OCI_LOGO);

		

	}
}
