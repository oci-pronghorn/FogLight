package com.ociweb.grove;

import static com.ociweb.iot.maker.Port.D7;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class RelayBehavior implements DigitalListener {

    private static final Port RELAY_PORT  = D7;

	
	final FogCommandChannel channel1;
	public RelayBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
    	channel1 = runtime.newCommandChannel();

	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		// TODO Auto-generated method stub
    	channel1.setValueAndBlock(RELAY_PORT, value == 1, 500);

	}

}
