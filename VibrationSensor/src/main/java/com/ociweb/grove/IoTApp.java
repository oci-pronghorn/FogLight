package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

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
		final FogCommandChannel ch = runtime.newCommandChannel();
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
				if (value < threshold){
					ch.setValue(BUZZER_PORT,false);
				}
				else {
					ch.setValueAndBlock(BUZZER_PORT, true,100);//set the buzzer_port high for at least 100ms
				}		
			
		}).includePorts(VIBRATION_SENSOR_PORT);
	}
}
