package com.ociweb.iot.pong;

import static com.ociweb.iot.grove.GroveTwig.AngleSensor;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;


public class IoTApp implements IoTSetup {

	public void declareConnections(Hardware c) {

		c.connectAnalog(AngleSensor, PongConstants.Player1Con);
		//c.useConnectI2C(new UltrasonicRangerTwig(PongConstants.Player2Con));

//		c.connectI2C(GroveLCDRGB)  TODO: begin logic goes here.
//		c.useI2C();
		
		c.setTriggerRate(50);
	//TOOD: 	c.startStateMachineWith(GameStage.???)


		//c.useConnectA(LightSensor, 1);

	}    

	public void declareBehavior(DeviceRuntime runtime){
		runtime.registerListener(new PongBehavior(runtime));
	}

	public static void main(String[] args) {        
		DeviceRuntime.run(new IoTApp());
	}

}