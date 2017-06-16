package com.ociweb;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.Grove_FourDigitDisplay;
import static com.ociweb.iot.grove.Grove_FourDigitDisplay.*;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements FogApp
{
	//private static final Port CLOCK = D5;
	//private static final Port DATA = D6;

	@Override
	public void declareConnections(Hardware c) {
		c.connect(FourDigitDisplay, CLOCK);
		c.connect(FourDigitDisplay, DATA);
		c.connect(Button, D2,50,false);
	

	}

	@Override
	public void declareBehavior(FogRuntime runtime) {

		final FogCommandChannel ch = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING,300);
		runtime.addDigitalListener((port, a,b, val)->{	
			if (val !=0){
				System.out.println("Pressed");
				//Grove_FourDigitDisplay.printDigitAt(ch, 0, 0, true);
				Grove_FourDigitDisplay.printDigitAt(ch, 2, 0);
			}
		}).includePorts(D2);

	}
}
