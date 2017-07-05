package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp {

	private static final Port LED_PORT = D3;
	private int lightIntensity = 0;
	public static void main( String[] args) {
		FogRuntime.run(new IoTApp());
	}    

	@Override
	public void declareConnections(Hardware hardware) {
		hardware.connect(LED, LED_PORT);
		hardware.setTriggerRate(50);
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
            runtime.addListener(new VariableColorLEDBehavior(runtime));
		         
	}
}

