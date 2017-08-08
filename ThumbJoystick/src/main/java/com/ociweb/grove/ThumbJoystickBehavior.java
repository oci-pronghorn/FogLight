package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.thumb_joystick.PressableJoystickListener;
import com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTransducer;
import static com.ociweb.iot.grove.thumb_joystick.ThumbJoystickTwig.*;
import com.ociweb.iot.maker.Port;

public class ThumbJoystickBehavior implements Behavior, PressableJoystickListener{
	private ThumbJoystickTransducer tj;
	
	public ThumbJoystickBehavior(Port p){
		tj = ThumbJoystick.newTransducer();
		tj.setPort(p).registerThumbJoystickListener(this); //can act as 'fluent' API
	}
	

	@Override
	public void joystickValues(int x, int y) {
		System.out.println("X: " + x + ", Y: " + y);
	}

	@Override
	public void pressed() {
		System.out.println("PRESSED!");
	}

	@Override
	public void released() {
		System.out.println("RELEASED!");
	}

}
