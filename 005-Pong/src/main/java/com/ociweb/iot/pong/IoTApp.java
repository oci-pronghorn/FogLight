package com.ociweb.iot.pong;

import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.Button;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import static com.ociweb.iot.maker.Port.*;


public class IoTApp implements IoTSetup {

	public void declareConnections(Hardware c) {

		c.connect(AngleSensor, PongConstants.Player1Con); //A1
		c.connect(AngleSensor, PongConstants.Player2Con); //A2
		c.connect(Button, PongConstants.ButtonCon); //D2
		
		
		c.setTriggerRate(100);
	//TODO: 	c.startStateMachineWith(GameStage.???)

	}    

	public void declareBehavior(DeviceRuntime runtime){
		runtime.registerListener(new PongBehavior(runtime));
	}

	public static void main(String[] args) {        
		DeviceRuntime.run(new IoTApp());
	}

}