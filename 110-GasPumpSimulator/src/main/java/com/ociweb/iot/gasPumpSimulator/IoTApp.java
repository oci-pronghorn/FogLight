package com.ociweb.iot.gasPumpSimulator;


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.UltrasonicRanger;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.A2;
import static com.ociweb.iot.maker.Port.D7;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.ListenerFilter;

public class IoTApp implements IoTSetup
{

	private static String serverURI;
	private static String clientId;
	private static int    tankDepth;
	private static String fuelName;
	private static int    centsPerGallon;
	private static boolean publishTankData;

	private final static int BUTTON_RATE_MS = 200; //5 per second per second
	private final static int RANGER_RATE_MS = 200; //5 per second per second

	private final static boolean RETURN_EVERY_SAMPLE = true; //by default only returns changes
	private final static String TOPIC_TANK  = "tank";
	private final static String TOPIC_PUMP  = "pump";
	private final static String TOPIC_TOTAL = "total";



    public static void main( String[] args ) {

    	fuelName =                             DeviceRuntime.getOptArg("--fuelName",         "-fn", args, "diesel");
    	centsPerGallon =  Integer.parseInt(    DeviceRuntime.getOptArg("--fuelPrice",        "-fp", args, "215"));
    	serverURI =                            DeviceRuntime.getOptArg("--brokerURI",        "-br", args, "tcp://127.0.0.1:1883");
    	clientId =                             DeviceRuntime.getOptArg("--clientId",         "-id", args, "unknownStation");
    	tankDepth =       Integer.parseInt(    DeviceRuntime.getOptArg("--tankDepth",        "-td", args, "13"));
    	publishTankData = Boolean.parseBoolean(DeviceRuntime.getOptArg("--publishTankData",  "-pt", args, "true"));

			DeviceRuntime.run(new IoTApp());

    }

    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, D7, BUTTON_RATE_MS, RETURN_EVERY_SAMPLE);
        c.connect(AngleSensor, A0);

				if (publishTankData) {
					  c.connect(UltrasonicRanger, A2, RANGER_RATE_MS, RETURN_EVERY_SAMPLE); //default rate 5x per second
				}

        c.startStateMachineWith(PumpState.Idle);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {

    	runtime.registerListener(new ModeSelector(runtime, AngleSensor.range()))
    	                              .includePorts(A0);

    	runtime.registerListener(new PumpSimulator(runtime, TOPIC_PUMP, TOPIC_TOTAL, fuelName, centsPerGallon))
    	                              .includePorts(D7);

    	runtime.registerListener(new DisplayController(runtime, TOPIC_TANK, TOPIC_PUMP, TOPIC_TOTAL))
    	                              .addSubscription(TOPIC_TANK)
    	                              .addSubscription(TOPIC_PUMP)
    	                              .addSubscription(TOPIC_TOTAL);

    	ListenerFilter mqttFilter = runtime.addPubSubListener(new PublishDataMQTT(serverURI,clientId))
   										              .addSubscription(TOPIC_TOTAL);

    	if (publishTankData) {
    	  	mqttFilter.addSubscription(TOPIC_TANK);
        	runtime.registerListener(new TankMonitor(runtime, TOPIC_TANK, tankDepth, fuelName))
        	                              .includePorts(A2);
    	}

    }

}
