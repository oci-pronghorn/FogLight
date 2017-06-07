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

	private static final Port VIBRATION_SENSOR_PORT = A0;
	private static final Port LED_PORT = D2;
	
	@Override
	public void declareConnections(Hardware c) {
		////////////////////////////
		//Connection specifications
		///////////////////////////

		c.connect(LED, LED_PORT);
		c.connect(VibrationSensor, VIBRATION_SENSOR_PORT);
		

	}


	@Override
	public void declareBehavior(DeviceRuntime runtime) {
		//////////////////////////////
		//Specify the desired behavior
		//////////////////////////////

		//  //Use lambdas or classes and add listeners to the runtime object
		//  //CommandChannels are created to send outgoing events to the hardware
		//  //CommandChannels must never be shared between two lambdas or classes.
		//  //A single lambda or class can use mulitiple CommandChannels for cuoncurrent behavior


		//        final CommandChannel channel1 = runtime.newCommandChannel();
		//        //this digital listener will get all the button press and un-press events 
		//        runtime.addDigitalListener((connection, time, value)->{ 
		//            
		//            //connection could be checked but unnecessary since we only have 1 digital source
		//            
		//            if (channel1.digitalSetValue(RELAY_PORT, value)) {
		//                //keep the relay on or off for 1 second before doing next command
		//                channel1.digitalBlock(RELAY_PORT, 1000); 
		//            }
		//        });

		final CommandChannel ch = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
		runtime.addAnalogListener((port, time, durationMillis, average, value)->{
			if (port == VIBRATION_SENSOR_PORT){
				if (value < 1000){
					ch.setValue(LED_PORT,0);
				}
				else {
					ch.setValue(LED_PORT, 1);
				}
				System.out.println(value);
			}

		});

	}


}
