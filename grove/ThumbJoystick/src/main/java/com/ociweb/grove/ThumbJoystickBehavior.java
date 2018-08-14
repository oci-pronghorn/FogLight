package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.thumb_joystick.PressableJoystickListener;
import com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTransducer;
import static com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTwig.*;
import com.ociweb.iot.maker.Port;

public class ThumbJoystickBehavior implements Behavior, PressableJoystickListener{
	private ThumbJoystickTransducer tj;
	
	public ThumbJoystickBehavior(Port p){
		tj = ThumbJoystick.newTransducer(p);
		tj.registerThumbJoystickListener(this);
	}
	

	@Override
	public void joystickValues(int x, int y) {
		System.out.println("X: " + x + ", Y: " + y);
	}

	
	@Override
	public void buttonStateChange(boolean pressed, long time,long previousDuration){
		System.out.println("Pressed:" + pressed + ", Duration: " + previousDuration);
	}

}
