package com.ociweb.iot.pong;

import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.LED;
import static com.ociweb.iot.grove.GroveTwig.LightSensor;
import static com.ociweb.iot.grove.GroveTwig.Relay;
import static com.ociweb.iot.grove.GroveTwig.RotaryEncoder;
import static com.ociweb.iot.grove.GroveTwig.AngleSensor;

import java.io.IOException;
import java.util.Arrays;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.grove.NunchuckTwig;
import com.ociweb.iot.grove.TempAndHumidTwig;
import com.ociweb.iot.grove.TempAndHumidTwig.MODULE_TYPE;
import com.ociweb.iot.grove.UltrasonicRangerTwig;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.iot.maker.CommandChannel;


public class IoTApp implements IoTSetup {

	public void declareConnections(Hardware c) {

		c.useConnectA(AngleSensor, 1);

		c.useTriggerRate(50);


		//        c.useConnectA(LightSensor, 1);

	}    

	public void declareBehavior(DeviceRuntime runtime){

		CommandChannel channel1 = runtime.newCommandChannel();
		
		runtime.addStartupListener(()->{
			Grove_LCD_RGB.begin(channel1);
		});
	}

	public static void main(String[] args) {        
		DeviceRuntime.run(new IoTApp());
	}

}