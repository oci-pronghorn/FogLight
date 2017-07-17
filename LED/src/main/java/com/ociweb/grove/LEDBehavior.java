package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class LEDBehavior implements DigitalListener {
	
	private static final Port LED_PORT = D2;
	
	final FogCommandChannel channel1;
	
	public LEDBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
    	channel1 = runtime.newCommandChannel();

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
    	channel1.setValueAndBlock(LED_PORT, value == 1, 200); 

	}

}
