package com.ociweb.iot.grove.thumb_joystick;

public interface PressableJoystickListener extends ThumbJoystickListener {
	void buttonStateChange(boolean pressed, long time,long previousDuration);
}
