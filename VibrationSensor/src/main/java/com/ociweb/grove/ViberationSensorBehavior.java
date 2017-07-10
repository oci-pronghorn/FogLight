package com.ociweb.grove;

import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class ViberationSensorBehavior implements AnalogListener {
	private static final int threshold = 800;
	private static final Port BUZZER_PORT = D2;

	final FogCommandChannel ch;

	public ViberationSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
		ch = runtime.newCommandChannel();
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		// TODO Auto-generated method stub

		if (value < threshold){
			ch.setValue(BUZZER_PORT,false);
		}
		else {
			ch.setValueAndBlock(BUZZER_PORT, true,100);//set the buzzer_port high for at least 100ms
		}
	}

}
