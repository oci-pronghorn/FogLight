package com.ociweb.iot.gasPumpSimulator;


import static com.ociweb.iot.grove.GroveTwig.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
	
	private static String serverURI;
	private static String clientId;
	private static int tankDepth;
	
	private final static int BUTTON_RATE_MS = 1000;
	private final static boolean RETURN_EVERY_SAMPLE = true; //by default only returns changes
	private final static String TOPIC_TANK = "tank";
	private final static String TOPIC_PUMP = "pump";
	
    public static void main( String[] args ) {
    	
    	serverURI = "127.0.0.1";
    	clientId = "me";
    	tankDepth = 13;
    	//may set prices of fuel
        DeviceRuntime.run(new IoTApp());
        
    }
        
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, D2, BUTTON_RATE_MS, RETURN_EVERY_SAMPLE);
        c.connect(AngleSensor, A1);
        c.connect(UltrasonicRanger, A0);
        
        c.startStateMachineWith(PumpState.Idle);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
    	runtime.addAnalogListener(new TankMonitor(runtime, TOPIC_TANK, tankDepth)).includePorts(A0);    	
    	runtime.addAnalogListener(new FuelSelector(runtime, AngleSensor.range())).includePorts(A1);
    	    	    	
    	registerFuelPumpSimulator(runtime, PumpState.PumpUnleaded);
    	registerFuelPumpSimulator(runtime, PumpState.PumpPlus);
    	registerFuelPumpSimulator(runtime, PumpState.PumpPremium);
    	     	
    	 //any sensors will only need to publish to upload to have the data relayed to the external service
    	runtime.addPubSubListener(new PublishDataMQTT(serverURI,clientId))
    										.addSubscription(TOPIC_PUMP)
    										.addSubscription(TOPIC_TANK);
    	

    	//add tank volume info on idle mode
    	
    	
    }

	private void registerFuelPumpSimulator(DeviceRuntime runtime, PumpState fuelState) {
		runtime.registerListener(new PumpSimulator(runtime, TOPIC_PUMP, fuelState))
    	                     				.includePorts(D2)
    	                     				.includeStateChangeTo(fuelState) //TODO: add to and from method to simplify
    	                     				.includeStateChangeFrom(fuelState);
	}
  
}
