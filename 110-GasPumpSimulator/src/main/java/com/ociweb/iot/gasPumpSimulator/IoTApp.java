package com.ociweb.iot.gasPumpSimulator;


import static com.ociweb.iot.grove.AnalogDigitalTwig.AngleSensor;
import static com.ociweb.iot.grove.AnalogDigitalTwig.Button;
import static com.ociweb.iot.grove.AnalogDigitalTwig.UltrasonicRanger;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.A2;
import static com.ociweb.iot.maker.Port.D7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.ListenerFilterIoT;

public class IoTApp implements FogApp
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


	private static final Logger logger = LoggerFactory.getLogger(IoTApp.class);

    public static void main( String[] args ) {

    	fuelName =                             FogRuntime.getOptArg("--fuelName",         "-fn", args, "diesel");
    	centsPerGallon =  Integer.parseInt(    FogRuntime.getOptArg("--fuelPrice",        "-fp", args, "215"));
    	serverURI =                            FogRuntime.getOptArg("--brokerURI",        "-br", args, "tcp://127.0.0.1:1883");
    	clientId =                             FogRuntime.getOptArg("--clientId",         "-id", args, "unknownStation");
    	tankDepth =       Integer.parseInt(    FogRuntime.getOptArg("--tankDepth",        "-td", args, "13"));
    	publishTankData = Boolean.parseBoolean(FogRuntime.getOptArg("--publishTankData",  "-pt", args, "true"));

			FogRuntime.run(new IoTApp());

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
    public void declareBehavior(FogRuntime runtime) {

    	runtime.registerListener(new ModeSelector(runtime, AngleSensor.range()))
    	                              .includePorts(A0);

    	runtime.registerListener(new PumpSimulator(runtime, TOPIC_PUMP, TOPIC_TOTAL, fuelName, centsPerGallon))
    	                              .includePorts(D7);

    	runtime.registerListener(new DisplayController(runtime, TOPIC_TANK, TOPIC_PUMP, TOPIC_TOTAL))
    	                              .addSubscription(TOPIC_TANK)
    	                              .addSubscription(TOPIC_PUMP)
    	                              .addSubscription(TOPIC_TOTAL);

    	ListenerFilterIoT mqttFilter = (ListenerFilterIoT) runtime.addPubSubListener(new PublishDataMQTT(serverURI,clientId))
   										              .addSubscription(TOPIC_TOTAL);

    	if (publishTankData) {
    	  	mqttFilter.addSubscription(TOPIC_TANK);
        	runtime.registerListener(new TankMonitor(runtime, TOPIC_TANK, tankDepth, fuelName))
        	                              .includePorts(A2);
    	} else {
    		logger.info("no pump attached");
    	}

    }

}
