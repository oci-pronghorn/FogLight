package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements FogApp
{
	private static final Port VIBRATION_SENSOR_PORT = A0;
	private static final Port BUZZER_PORT = D2;
	private static final int threshold = 800;
	
	@Override
	public void declareConnections(Hardware c) {
		c.connect(Buzzer, BUZZER_PORT);
		c.connect(VibrationSensor, VIBRATION_SENSOR_PORT);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
			if (port == VIBRATION_SENSOR_PORT){
				if (value < threshold){
					ch.setValue(BUZZER_PORT,0);
				}
				else {
					ch.setValue(BUZZER_PORT, 1);
				}		
			}
		});
	}
}
