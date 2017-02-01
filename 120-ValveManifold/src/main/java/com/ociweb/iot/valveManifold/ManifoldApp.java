package com.ociweb.iot.valveManifold;

import java.util.concurrent.TimeUnit;

import com.ociweb.iot.valveManifold.schema.ValveSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.monitor.MonitorConsoleStage;
import com.ociweb.pronghorn.stage.route.ReplicatorStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;
import com.ociweb.pronghorn.stage.test.ConsoleJSONDumpStage;

public class ManifoldApp {

	private static final PipeConfig<ValveSchema> valveDataPipeConfig = new PipeConfig<ValveSchema>(ValveSchema.instance, 200, 64);
	private static final PipeConfig<RawDataSchema> uartBytesPipeConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 200, 512);
	
	private static ManifoldApp instance;	
	private final GraphManager gm;
	private final int rateInNS = 200_000; 
	
	public ManifoldApp() {
		
		gm = new GraphManager();
		GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, rateInNS); 		
				
	}	
	
	public static void main(String[] args) {
		
		instance = new ManifoldApp();
		instance.buildGraph();
		instance.runGraph();
		
	}	
	
	public void buildGraph() {
		
		Pipe<RawDataSchema> uartBytesPipe = new Pipe<RawDataSchema>(uartBytesPipeConfig);
		Pipe<ValveSchema> valveDataPipe = new Pipe<ValveSchema>(valveDataPipeConfig);
		
		UARTDataStage.instance(gm, uartBytesPipe);
		ValveDataParserStage.instance(gm, uartBytesPipe, valveDataPipe);	
		
		new ConsoleJSONDumpStage(gm, valveDataPipe);
		
		//MQTTPublishPAHOStage.instance(gm, valveDataPipe,"tcp://127.0.0.1:1883","42");
				
		MonitorConsoleStage.attach(gm);
	}
	
	public void runGraph() {
		
		StageScheduler scheduler = new ThreadPerStageScheduler(gm);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	scheduler.shutdown();
		    	scheduler.awaitTermination(3, TimeUnit.SECONDS);
		    }
		});
		
		scheduler.startup();		
		
	}
	
	
	
}
