package com.ociweb.iot.valveManifold;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.HTTPServer;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.monitor.MonitorConsoleStage;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;
import com.ociweb.pronghorn.stage.test.PipeCleanerStage;

public class ManifoldApp {
	
	private static final int maxMSG = 30 * 10 * 60; //one min of data for 10 values.	

	private static final PipeConfig<RawDataSchema> uartBytesPipeConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 100, 400);
	private static final PipeConfig<ValveSchema> valveDataPipeConfig = new PipeConfig<ValveSchema>(ValveSchema.instance, maxMSG, 100);
	private static final Logger logger = LoggerFactory.getLogger(ManifoldApp.class);
	
	private static ManifoldApp instance;	
	private final GraphManager gm;
	private final int rateInNS = 2_000; 
	private final String gatewayHost;
	private final String clientId;
	
	public ManifoldApp(String gatewayHost, String clientId) {
		
		this.gatewayHost = gatewayHost;
		this.clientId = clientId;
		
		gm = new GraphManager();
		GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, rateInNS); 		
				
	}	
	
	public static void main(String[] args) {
		
		String gatewayHost = HTTPServer.getOptArg("-host", "--h", args, "127.0.0.1");	
		String clientId = HTTPServer.getOptArg("-clientName", "--n", args, "UnknownManifold");	
		String runSimulation = HTTPServer.getOptArg("-simulation", "--s", args, "False");
		
		instance = new ManifoldApp(gatewayHost, clientId);
		instance.buildGraph(Boolean.parseBoolean(runSimulation));
		instance.runGraph();
				
	}	
	
	public void buildGraph(boolean simulate) {
		//logger.info("build graph");
		
		if (!simulate) {
			
			Pipe<RawDataSchema> uartBytesPipe = new Pipe<RawDataSchema>(uartBytesPipeConfig);
			Pipe<ValveSchema> valveDataPipe = new Pipe<ValveSchema>(valveDataPipeConfig);
			
			UARTDataStage.newInstance(gm, uartBytesPipe); //take the raw data off the UART and put it on the pipe		
			
			ValveDataParserStage.newInstance(gm, uartBytesPipe, valveDataPipe); //parse the raw data and send messages	
			
			MQTTPublishPAHOStage.newInstance(gm, valveDataPipe,"tcp://"+gatewayHost+":1883",clientId);//send data to the gateway
			
			
		} else {
			//build up simulators
	
			singleSimulatedManifold("1", 5, 10);
			singleSimulatedManifold("2", 5, 1000);
			singleSimulatedManifold("3", 5, 10000);
			singleSimulatedManifold("4", 5, 100000);
			singleSimulatedManifold("5", 5, 1000000);
						
//			The manifold ids are 1, 2, 3, 4, and 5.
//			The one they focus on is in the lower left corner and is id 4.
//			The valve ids go from 0 to 5 on each manifold.
		}
		

		
	}

	private void singleSimulatedManifold(String client, int valves, int base) {
		Pipe<ValveSchema> valveDataPipe = new Pipe<ValveSchema>(valveDataPipeConfig);			
		Pipe<RawDataSchema> uartBytesPipe = new Pipe<RawDataSchema>(uartBytesPipeConfig);
		
		SimulatedUARTDataStage.newInstance(gm, uartBytesPipe, valves, base); //for making fake data			
		ValveDataParserStage.newInstance(gm, uartBytesPipe, valveDataPipe); //parse the raw data and send messages	
		
		MQTTPublishPAHOStage.newInstance(gm, valveDataPipe, "tcp://"+gatewayHost+":1883", client);//send data to the gateway
	}
	
	public void runGraph() {
		//logger.info("run graph");
		
		StageScheduler scheduler = new ThreadPerStageScheduler(gm);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	scheduler.shutdown();
		    	scheduler.awaitTermination(3, TimeUnit.SECONDS);
		    }
		});
		
		scheduler.startup();		
		//logger.info("running graph");
	}
	
	public static String reportChoice(final String longName, final String shortName, final String value) {
	    System.out.print(longName);
	    System.out.print(" ");
	    System.out.print(shortName);
	    System.out.print(" ");
	    System.out.println(value);
	    return value;
	}


	public static String getOptArg(String longName, String shortName, String[] args, String defaultValue) {
	    
	    String prev = null;
	    for (String token : args) {
	        if (longName.equals(prev) || shortName.equals(prev)) {
	            if (token == null || token.trim().length() == 0 || token.startsWith("-")) {
	                return defaultValue;
	            }
	            return reportChoice(longName, shortName, token.trim());
	        }
	        prev = token;
	    }
	    return reportChoice(longName, shortName, defaultValue);
	}
	
}
