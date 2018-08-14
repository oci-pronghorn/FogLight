package com.ociweb.grove;

import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.FogCommandChannel.*;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class TouchSensorBehavior implements DigitalListener {

	private static final Port LED_PORT = D2;

	final FogCommandChannel channel1;
	public TouchSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
    	channel1 = runtime.newCommandChannel(PIN_WRITER);

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
        channel1.setValueAndBlock(LED_PORT, value == 1, 500);                                                                            //delays a future action

	}

}
