package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.MsgCommandChannel;

public class IoTApp implements FogApp
{
	private static final Port THUMBJOYSTICK_PORT_X = A0;
	private static final Port THUMBJOYSTICK_PORT_Y = A1;
	private static final String GroveTwig = null;

	@Override
	public void declareConnections(Hardware c) {

		
		//TODO: pinUsed() is not automatically allowing user to automatically connect both ports once one port is connected
		c.connect(ThumbJoystick, THUMBJOYSTICK_PORT_X);
		c.connect(ThumbJoystick, THUMBJOYSTICK_PORT_Y);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
			if ( port == THUMBJOYSTICK_PORT_X){
				//the X value should be roughly between 200 to 800 unless pressed
				if (!ThumbJoystick.isPressed(value)){
					System.out.println("X: "+value);
				}
				else {
					System.out.println("Pressed");
				}
			}
			
			else if (port == THUMBJOYSTICK_PORT_Y){
				System.out.println("Y: "+value);

			}
		});
	}
}
