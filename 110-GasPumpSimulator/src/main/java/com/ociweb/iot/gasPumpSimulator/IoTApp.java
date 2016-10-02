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
	private static int    tankDepth;
	private static String fuelName;
	private static int    centsPerGallon; 
	
	private final static int BUTTON_RATE_MS = 100; //10 per second per second
	private final static boolean RETURN_EVERY_SAMPLE = true; //by default only returns changes
	private final static String TOPIC_TANK  = "tank";
	private final static String TOPIC_PUMP  = "pump";
	private final static String TOPIC_TOTAL = "total";
	
	
	
    public static void main( String[] args ) {
    	
    	fuelName =                        DeviceRuntime.getOptArg("--fuelName", "-fn", args, "Unleaded");
    	centsPerGallon = Integer.parseInt(DeviceRuntime.getOptArg("--fuelName", "-fn", args, "215"));    	    	
    	serverURI =                       DeviceRuntime.getOptArg("--brokerURI", "-br", args, "tcp://127.0.0.1");
    	clientId =                        DeviceRuntime.getOptArg("--clientId", "-id", args, "unknown");	    			
    	tankDepth =      Integer.parseInt(DeviceRuntime.getOptArg("--tankDepth", "-td", args, "13"));

        DeviceRuntime.run(new IoTApp());
        
    }
        
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, D7, BUTTON_RATE_MS, RETURN_EVERY_SAMPLE);
        c.connect(AngleSensor, A0);
        c.connect(UltrasonicRanger, A2); //default rate 5x per second
        
        c.startStateMachineWith(PumpState.Idle);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
    	//Continuous monitoring of the tank and values published to TOPIC_TANK    	
    	runtime.registerListener(new TankMonitor(runtime, TOPIC_TANK, tankDepth, fuelName))
    	                              .includePorts(A2);
    	
    	runtime.registerListener(new ModeSelector(runtime, AngleSensor.range()))
    	                              .includePorts(A0);
    	  	    	
    	runtime.registerListener(new PumpSimulator(runtime, TOPIC_PUMP, TOPIC_TOTAL, fuelName, centsPerGallon))
    	                              .includePorts(D7);
    	     	    	
    	runtime.registerListener(new DisplayController(runtime, TOPIC_TANK, TOPIC_PUMP, TOPIC_TOTAL))
    	                              .addSubscription(TOPIC_TANK)
    	                              .addSubscription(TOPIC_PUMP)
    	                              .addSubscription(TOPIC_TOTAL);
    	                              

    	
    	//TODO: enable this last.
    	 //any sensors will only need to publish to upload to have the data relayed to the external service
//    	runtime.addPubSubListener(new PublishDataMQTT(serverURI,clientId))
//    										.addSubscription(TOPIC_PUMP)
//    										.addSubscription(TOPIC_TANK);
    	

    	//add tank volume info on idle mode
    	
    	
    }

  
}
