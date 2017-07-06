package com.ociweb.iot.pong;

import static com.ociweb.iot.grove.AnalogDigitalTwig.AngleSensor;
import static com.ociweb.iot.grove.AnalogDigitalTwig.Button;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import static com.ociweb.iot.maker.Port.*;


public class IoTApp implements FogApp {

	public void declareConnections(Hardware c) {

		c.connect(AngleSensor, PongConstants.Player1Con); //A1
		c.connect(AngleSensor, PongConstants.Player2Con); //A2
		c.connect(Button, PongConstants.ButtonCon); //D2
		
		
		c.setTriggerRate(100);
	//TODO: 	c.startStateMachineWith(GameStage.???)

	}    

	public void declareBehavior(FogRuntime runtime){
		runtime.registerListener(new PongBehavior(runtime));
	}

	public static void main(String[] args) {        
		FogRuntime.run(new IoTApp());
	}

}