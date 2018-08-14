package com.ociweb.grove;

import com.ociweb.gl.api.TimeListener;
import static com.ociweb.iot.grove.oled.OLEDTwig.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.grove.oled.OLED_96x96_Transducer;
import static com.ociweb.grove.OCI_Logo.*;
import static com.ociweb.grove.Grumpy.*;
import static com.ociweb.grove.PiLogo.*;
import static com.ociweb.grove.DexterLogo.*;
import static com.ociweb.grove.QR.*;
import static com.ociweb.grove.Huy.*;


public class OLED_96x96Behavior implements TimeListener{
	
	private final OLED_96x96_Transducer display;
	public OLED_96x96Behavior(FogRuntime rt){
		display = OLED_96x96.newTransducer(rt.newCommandChannel()); 

	}
	@Override
	public void timeEvent(long time, int iteration) {
		
		int remainder = iteration % 6;

		switch (remainder){
			case 0:
				display.display(OCI_LOGO);
				break;
			case 1:
				display.display(GRUMPY);
				break;
			case 2:
				display.display(PI_LOGO);
				break;
			case 3:
				display.display(DEX_LOGO);
				break;
			case 4:
				display.display(HUY);
			case 5:
				display.display(OCI_LINK);
			}
		
		System.out.println("Switching to image " + remainder);
		
	}
}
