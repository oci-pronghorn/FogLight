package com.ociweb.iot.pong;

import static com.ociweb.iot.grove.GroveTwig.AngleSensor;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import static com.ociweb.iot.maker.Port.*;


public class IoTApp implements IoTSetup {

	public void declareConnections(Hardware c) {

		c.connect(AngleSensor, PongConstants.Player1Con);
		c.connect(AngleSensor, PongConstants.Player2Con);
		//c.useConnectI2C(new UltrasonicRangerTwig(PongConstants.Player2Con));

//		c.connectI2C(GroveLCDRGB)  TODO: begin logic goes here.
//		c.useI2C();
		
		c.setTriggerRate(100);
	//TODO: 	c.startStateMachineWith(GameStage.???)


		//c.useConnectA(LightSensor, 1);

	}    

	public void declareBehavior(DeviceRuntime runtime){
		runtime.registerListener(new PongBehavior(runtime));
	}

	public static void main(String[] args) {        
		DeviceRuntime.run(new IoTApp());
	}

}