package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup
{
	///////////////////////
	//Connection constants 
	///////////////////////
	// // by using constants such as these you can easily use the right value to reference where the sensor was plugged in

	private static final Port THUMBJOYSTICK_PORT_Y = A0;
	private static final Port THUMBJOYSTICK_PORT_X = A1;

	@Override
	public void declareConnections(Hardware c) {
		////////////////////////////
		//Connection specifications
		///////////////////////////

		// // specify each of the connections on the harware, eg which component is plugged into which connection.        

		
		//TODO: pinUsed() is not automatically allowing user to automatically connect both ports once one port is connected
		c.connect(ThumbJoystick, THUMBJOYSTICK_PORT_Y);
		c.connect(ThumbJoystick, THUMBJOYSTICK_PORT_X);
	}


	@Override
	public void declareBehavior(DeviceRuntime runtime) {
		final CommandChannel channel1 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
			switch (port){
			case A0:
				//the X value should be roughly between 200 to 800 unless pressed
				if (value < 1023){
					System.out.println("X: "+value);
				}
				else {
					System.out.println("Pressed");
				}
				break;
			case A1:
				System.out.println("Y: "+value);
				break;
			default:
				System.out.println("Please ensure that you are connecting to the correct port (A0)");
				break;
			}
		});
	}


}
