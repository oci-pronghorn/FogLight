package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;
import com.ociweb.gl.api.MsgCommandChannel;

public class IoTApp implements FogApp
{
	private static final Port THUMBJOYSTICK_PORT = A0;

	@Override
	public void declareConnections(Hardware c) {
		//TODO: pinUsed() is not automatically allowing user to automatically connect both ports once one port is connected
		c.connect(ThumbJoystick, THUMBJOYSTICK_PORT);
	}


	@Override
	public void declareBehavior(FogRuntime runtime) {
		runtime.registerListener(new ThumbJoystickBehavior(THUMBJOYSTICK_PORT));
	}
}
