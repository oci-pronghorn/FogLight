package com.ociweb.grove;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;
import static com.ociweb.iot.maker.FogCommandChannel.*;

public class LightSensorBehavior implements AnalogListener {
	
	private static final Port LED_PORT = D2;
    private static final int darkValue = 350; //Light reading that will turn on the light

	final FogCommandChannel lcdScreenChannel;

	public LightSensorBehavior(FogRuntime runtime) {
		// TODO Auto-generated constructor stub
		lcdScreenChannel = runtime.newCommandChannel(PIN_WRITER);
	}

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		// TODO Auto-generated method stub
		lcdScreenChannel.setValue(LED_PORT, value < darkValue);
		System.out.println(value);
	}

}
