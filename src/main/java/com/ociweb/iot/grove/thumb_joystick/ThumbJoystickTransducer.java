package com.ociweb.iot.grove.thumb_joystick;

import com.ociweb.iot.maker.IODeviceTransducer;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.transducer.AnalogListenerTransducer;
import static com.ociweb.iot.maker.Port.*;

import java.util.ArrayList;

public class ThumbJoystickTransducer implements AnalogListenerTransducer, IODeviceTransducer{

	private Port port = A1; //we default to A1 for X and A2 for Y
	private int x = -1;
	private int y = -1;
	private long lastButtonStateChangeTime = -1L;
	private Z z = Z.NotPressed;

	private final int PRESSED_X_VALUE = 1023;

	private ArrayList <ThumbJoystickListener> listeners = new ArrayList<ThumbJoystickListener>();

	public ThumbJoystickTransducer(){
	}

	public ThumbJoystickTransducer(Port p){
		port = p;
	}
	public ThumbJoystickTransducer registerThumbJoystickListener(ThumbJoystickListener... ls){
		for (ThumbJoystickListener l: ls){
			listeners.add(l);
		}
		return this;
	}

	/**
	 * This port and its upper neighbour port (although not physically plugged in) will be the ports where X and Y are reported.
	 * @param p
	 */
	public ThumbJoystickTransducer setPort(Port p){
		port = p;
		return this;
	}

	/**
	 * Updates X and Y values accordingly and fires off all of the appropriate thumbJoystickValue events for its registered listeners.
	 */

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		if (port.port == (this.port.port)){
			x = value;
			if (x == PRESSED_X_VALUE){
				if (z == Z.NotPressed){
					fireButtonStateChangeEvents(true, time, time - lastButtonStateChangeTime);
					lastButtonStateChangeTime = time;
					z = Z.Pressed;
				}
				return; //we will not be triggering joystickValues listeners events because x is 1023.
			}
			else if (x != PRESSED_X_VALUE && z == Z.Pressed){
				fireButtonStateChangeEvents(false, time, time - lastButtonStateChangeTime);
				lastButtonStateChangeTime = time;
				z = Z.NotPressed;
			}

		}
		else if (port.port == (this.port.port + 1)){
			y = value;
		}

		if (x != -1 && y != -1){
			for (ThumbJoystickListener l: listeners){
				((ThumbJoystickListener) l).joystickValues(x, y);

			}
		}
	}

	private void fireButtonStateChangeEvents(boolean pressed, long duration, long time){
		int i = listeners.size();
		while (--i >= 0){
			if (listeners.get(i) instanceof PressableJoystickListener){
				((PressableJoystickListener) listeners.get(i)).buttonStateChange(pressed, duration, time);
			}
		}
	}
	private enum Z {Pressed, NotPressed};
}
