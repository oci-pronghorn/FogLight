package com.ociweb.iot.grove.thumb_joystick;

import com.ociweb.iot.maker.Port;
import com.ociweb.iot.transducer.AnalogListenerTransducer;
import static com.ociweb.iot.maker.Port.*;

import java.util.ArrayList;
public class ThumbJoystickTransducer implements AnalogListenerTransducer{
	private Port port = A1; //we default to A1 for X and A2 for Y
	private int x = -1;
	private int y = -1;
	private Z z = Z.NotPressed;
	private final int PRESSED_X_VALUE = 1023;
	private ArrayList <ThumbJoystickListener> listeners;

	public ThumbJoystickTransducer(){

	}

	public void registerThumbJoystickListener(ThumbJoystickListener... ls){
		for (ThumbJoystickListener l: ls){
			listeners.add(l);
		}
	}

	/**
	 * This port and its upper neighbour port (although not physically plugged in) will be the ports where X and Y are reported.
	 * @param p
	 */
	public void setPort(Port p){
		port = p;
	}

	/**
	 * Updates X and Y values accordingly and fires off all of the appropriate thumbJoystickValue events for its registered listeners.
	 */

	@Override
	public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
		if (x == PRESSED_X_VALUE){
			if (z == Z.NotPressed){
				firePressedJoystickEvents();
				z = Z.Pressed;
			}
			return; //we will not be triggering joystickValues listeners events because x is 1023.
		}
		else {
			if (z == Z.Pressed){
				fireReleasedJoystickEvents();
				z = Z.NotPressed;
			}
		}


		if (port.port == (this.port.port)){
			x = value;
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

	private void firePressedJoystickEvents(){
		for (ThumbJoystickListener l:listeners){
			if ( l instanceof PressableJoystickListener){
				((PressableJoystickListener) l).pressed();
			}
		}
	}

	private void fireReleasedJoystickEvents(){
		for (ThumbJoystickListener l:listeners){
			if ( l instanceof PressableJoystickListener){
				((PressableJoystickListener) l).released();
			}
		}
	}

	private enum Z {Pressed, NotPressed};
}
