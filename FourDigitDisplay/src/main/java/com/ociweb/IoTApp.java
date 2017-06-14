package com.ociweb;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.Grove_FourDigitDisplay;
import com.ociweb.iot.grove.display.FourDigitDisplay;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements FogApp
{
	private static final Port CLOCK = D5;
	private static final Port DATA = D6;

	@Override
	public void declareConnections(Hardware c) {
	//	c.connect(FourDigitDisplay, CLOCK);
	//	c.connect(FourDigitDisplay, DATA);
		c.connect(Button, D2,5000,false);
		c.enableTelemetry(true);

	}

	@Override
	public void declareBehavior(FogRuntime runtime) {

		final FogCommandChannel ch = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		final FourDigitDisplay dis = new FourDigitDisplay(ch);
		runtime.addDigitalListener((port, a,b, val)->{		
			Grove_FourDigitDisplay.printDigitAt(ch, 1, 0, true);
			Grove_FourDigitDisplay.printDigitAt(ch, 2, 1, true);
			Grove_FourDigitDisplay.printDigitAt(ch, 3, 2, true);
			Grove_FourDigitDisplay.printDigitAt(ch, 4, 3, true);
			
		}).includePorts(D2);
		
	}


}
