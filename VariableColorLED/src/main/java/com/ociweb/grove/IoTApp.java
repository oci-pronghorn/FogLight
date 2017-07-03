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

		final FogCommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING);

		runtime.addTimeListener((time,iteration)->{
				lightIntensity = (int) (127* Math.sin(time/(Math.PI * 500)) + 127);
				System.out.println(lightIntensity);
				ledChannel.setValue(LED_PORT, lightIntensity);
		});            
	}
}

