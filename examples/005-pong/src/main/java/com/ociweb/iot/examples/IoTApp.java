package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.AngleSensor;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp {

	public void declareConnections(Hardware c) {

		c.connect(AngleSensor, PongConstants.Player1Con); //A1
		c.connect(AngleSensor, PongConstants.Player2Con); //A2
		c.connect(Button, PongConstants.ButtonCon); //D2
		
		
		c.setTimerPulseRate(100);
	//TODO: 	c.startStateMachineWith(GameStage.???)

	}    

	public void declareBehavior(FogRuntime runtime){
		runtime.registerListener(new PongBehavior(runtime));
	}

	public static void main(String[] args) {        
		FogRuntime.run(new IoTApp());
	}

}