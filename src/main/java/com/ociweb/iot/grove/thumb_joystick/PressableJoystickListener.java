package com.ociweb.iot.grove.thumb_joystick;

public interface PressableJoystickListener extends ThumbJoystickListener {
	void pressed();
	void released();
}
