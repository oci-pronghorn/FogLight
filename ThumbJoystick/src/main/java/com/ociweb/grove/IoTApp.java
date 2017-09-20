package com.ociweb.grove;


import static com.ociweb.iot.maker.Port.A0;

import com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTwig;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
	private static final Port THUMBJOYSTICK_PORT = A0; //TODO: test and fix with A2 should work...

	@Override
	public void declareConnections(Hardware c) {
		//TODO: pinUsed() is not automatically allowing user to automatically connect both ports once one port is connected
		c.connect(ThumbJoystickTwig.ThumbJoystick, THUMBJOYSTICK_PORT);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new ThumbJoystickBehavior(THUMBJOYSTICK_PORT));
	}
	public static void main (String[] args){
		FogRuntime.run(new IoTApp());
	}
}
