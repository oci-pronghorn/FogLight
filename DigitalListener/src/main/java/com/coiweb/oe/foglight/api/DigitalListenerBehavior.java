package com.coiweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class DigitalListenerBehavior implements DigitalListener {

	private static final Port BUZZER_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private static final Port TOUCH_SENSOR_PORT = D4;

	private FogCommandChannel channel;
	public DigitalListenerBehavior(FogRuntime runtime) {
		channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER | DYNAMIC_MESSAGING);
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		if(value == 1){
			channel.setValue(BUZZER_PORT, true);
			System.out.println("Digital event came from " + port);
			
			
		}
		else{
			channel.setValue(BUZZER_PORT, false);
			System.out.println("Buzzer was on for " + durationMillis + " milliseconds");
			System.out.println("time: " + time);
		}
	}

}
