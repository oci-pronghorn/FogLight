package com.ociweb;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.StaticFourDigitDisplay;
import com.ociweb.iot.grove.display.FourDigitDisplay;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup
{
	private static final Port CLOCK = D2;
	private static final Port DATA = D3;

	@Override
	public void declareConnections(Hardware c) {
	//	c.connect(FourDigitDisplay, CLOCK);
		//c.connect(FourDigitDisplay, DATA);

	}
	public static void main(String args[]){
		DeviceRuntime.run(new IoTApp());
	}
	
	@Override
	public void declareBehavior(DeviceRuntime runtime) {

		final CommandChannel channel1 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		
		
		//TODO: abstract the object dynamic allocation ("new") away from the maker
		FourDigitDisplay dis = new FourDigitDisplay(channel1);
		dis.printDigitAt(1, 0);
		dis.printDigitAt(2, 1);
		dis.printDigitAt(3, 2);
		dis.printDigitAt(1, 3);
		

	}


}
