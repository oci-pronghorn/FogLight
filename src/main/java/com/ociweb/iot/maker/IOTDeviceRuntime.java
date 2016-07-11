package com.ociweb.iot.maker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.GroveV2PiImpl;
import com.ociweb.iot.hardware.GroveV3EdisonImpl;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.TestHardware;
import com.ociweb.pronghorn.iot.EdisonReactiveListenerStage;
import com.ociweb.pronghorn.iot.PiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.util.Appendables;

public class IOTDeviceRuntime {

    //TODO: we may need a static singleton accessory for this.
    
    /*
     * Caution: in order to make good use of ProGuard we need to make an effort to avoid using static so 
     * dependencies can be traced and kept in the jar.
     *  
     */
    private static final Logger logger = LoggerFactory.getLogger(IOTDeviceRuntime.class);
    
    protected Hardware hardware;
    
    private StageScheduler scheduler;
    private final GraphManager gm;
  
    
    private final PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 128);
    private final PipeConfig<TrafficOrderSchema> goPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, 64);    
    private final PipeConfig<I2CCommandSchema> i2cPayloadPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 64,1024);    
    private final PipeConfig<I2CResponseSchema> reponseI2CConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, 64, 1024);
    private final PipeConfig<GroveResponseSchema> responsePipeConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 64);    
    private final PipeConfig<TrafficOrderSchema> orderPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, 64);

    
    private boolean isEdison = false;
    private boolean isPi = false;
    private List<Pipe> pipesForListenerConsumption = new ArrayList<Pipe>(); 
    
    private int DEFAULT_SLEEP_RATE_NS = 20_000_000; //we will only check for new work 50 times per second to keep CPU usage low.
    
    private static final byte piI2C = 1;
    private static final byte edI2C = 6;
    
    
    public IOTDeviceRuntime() {
        gm = new GraphManager();
        //by default, unless explicitly set the stages will use this sleep rate
        GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, DEFAULT_SLEEP_RATE_NS);       
        
    }

    
    public Hardware getHardware(){
    	if(this.hardware==null){
    	    
    	    ////////////////////////
    	    //The best way to detect the pi or edison is to first check for the expected matching i2c implmentation
    	    ///////////////////////
    	    
    	    I2CBacking i2cBacking = null;
    	    
    	    i2cBacking = Hardware.getI2CBacking(piI2C);
    	    if (null != i2cBacking) {
    	        this.isPi = true;
    	        this.hardware = new GroveV2PiImpl(gm, i2cBacking);
    	        System.out.println("Detected running on Pi");
    	    } else {
    	        i2cBacking = Hardware.getI2CBacking(edI2C);
    	        if (null != i2cBacking) {
    	            this.isEdison = true;
    	            this.hardware = new GroveV3EdisonImpl(gm, i2cBacking);
    	            System.out.println("Detected running on Edison");
    	        } else {
    	            this.hardware = new TestHardware(gm);
    	            System.out.println("Unrecognized hardware, test mock hardware will be used");
    	        }    	        
    	    }  	    
    	    
    	}
    	return this.hardware;
    }
    
    public CommandChannel newCommandChannel() { 
         
    	return this.hardware.newCommandChannel(new Pipe<GroveRequestSchema>(requestPipeConfig ),
    	                                       new Pipe<I2CCommandSchema>(i2cPayloadPipeConfig), 
    	                                       new Pipe<TrafficOrderSchema>(goPipeConfig));
    	
    }

    
    public void addRotaryListener(RotaryListener listener) {
        registerListener(listener);
    }
    
    public void addStartupListener(StartupListener listener) {
        registerListener(listener);
    }
        
    public void addAnalogListener(AnalogListener listener) {
        registerListener(listener);
    }
    
    public void addDigitalListener(DigitalListener listener) {  
    	System.out.println("Creating a Digital Listener");
        registerListener(listener);
    }
    
    public void addTimeListener(TimeListener listener) {
        registerListener(listener);
    }
        
    public void addI2CListener(I2CListener listener) {
        registerListener(listener);
    }
    
    public void registerListener(Object listener) {
        
        
        if(isPi){ // TODO: more Grove Specific stuff. Needs to change to add GPIO read support
        	System.out.println("Creating Pi Listener");
        	assert(!isEdison);
	        if (listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener) {
	            Pipe<I2CResponseSchema> pipe = new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x());
	            System.out.println("added new Pi Pipe");
	            pipesForListenerConsumption.add(pipe);
	        }
        }else if(isEdison){
        	System.out.println("Creating Edison Listener");
        	assert(!isPi);
        	if (listener instanceof I2CListener) {
	            Pipe<I2CResponseSchema> pipe = new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x());
	            pipesForListenerConsumption.add(pipe);
	        }
	        else if (listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener) {
	            Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
	            pipesForListenerConsumption.add(pipe);
	            System.out.println("added new response pipe and added lit edison listener");
	        }
        }else{ //I just left it as is maybe doesn't need to be different from Ed
        	System.out.println("Creating Mock Listener");
        	if (listener instanceof I2CListener) {
	            Pipe<I2CResponseSchema> pipe = new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x());
	            pipesForListenerConsumption.add(pipe);
	        }
	        else if (listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener) {
	            Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
	            pipesForListenerConsumption.add(pipe);
	            System.out.println("added new response pipe and added lit edison listener");
	        }
        }
        
        
        /////////////////////
        //StartupListener is not driven by any response data and is called when the stage is started up. no pipe needed.
        /////////////////////
        //TimeListener, time rate signals are sent from the stages its self and therefore does not need a pipe to consume.
        /////////////////////
        if (listener instanceof PubSubListener) {
            //TODO: need to implement
        }
        if (listener instanceof RestListener) {
            //TODO: need to implement
        }
        
        
        if(isPi){
        	System.out.println("Creating new PiReactiveListenerStage with pipe array size "+pipesForListenerConsumption.toArray(new Pipe[pipesForListenerConsumption.size()]).length);
        	configureStageRate(listener, new PiReactiveListenerStage(gm, listener, pipesForListenerConsumption.toArray(new Pipe[pipesForListenerConsumption.size()]))); 
        }else{
        	configureStageRate(listener, new EdisonReactiveListenerStage(gm, listener, pipesForListenerConsumption.toArray(new Pipe[pipesForListenerConsumption.size()])));
        }
    }

    protected void configureStageRate(Object listener, ReactiveListenerStage stage) {
        //if we have a time event turn it on.
        long rate = hardware.getTriggerRate();
        if (rate>0 && listener instanceof TimeListener) {
            stage.setTimeEventSchedule(rate);
            //Since we are using the time schedule we must set the stage to be faster
            long customRate =   (rate*1_000_000)/4;// in ns and 4x faster than clock trigger
            GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, Math.min(customRate,DEFAULT_SLEEP_RATE_NS),stage);
        }
    }

        
    public void start() {
       hardware.coldSetup();
   
       hardware.buildStages(GraphManager.allPipesOfType(gm, GroveRequestSchema.instance), 
                            GraphManager.allPipesOfType(gm, I2CCommandSchema.instance),
                            GraphManager.allPipesOfType(gm, GroveResponseSchema.instance), 
                            GraphManager.allPipesOfType(gm, TrafficOrderSchema.instance), 
                            GraphManager.allPipesOfType(gm, I2CResponseSchema.instance));
    
       
       //find all the instances of CommandChannel stage to startup first, note they are also unscheduled.
            
       
       exportGraphDotFile();      
       
       scheduler = hardware.createScheduler(this);
       scheduler.startup();

    }
    
    //TODO: required for testing only
    public StageScheduler getScheduler() {
        return scheduler;
    }
    
    /**
     * Export file so GraphVis can be used to view the internal graph.
     * 
     * To view this file install:     sudo apt-get install graphviz
     * 
     */
    private void exportGraphDotFile() {
        FileOutputStream fost;
        try {
            fost = new FileOutputStream("deviceGraph.dot");
            PrintWriter pw = new PrintWriter(fost);
            gm.writeAsDOT(gm, pw);
            pw.close();
            
            
            //to produce the png we must call
            //  dot -Tpng -O deviceGraph.dot        
            Process result;
            result = Runtime.getRuntime().exec("dot -Tpng -O deviceGraph.dot");
            
            if (0==result.waitFor()) {
                System.out.println("Built deviceGraph.dot.png to view the runtime graph.");
            }
        } catch (Exception e) {
            logger.debug("No runtime graph produced.",e);;
            System.out.println("No runtime graph produced.");
        }
        
        
    }

    public void shutdownDevice() {
        //clean shutdown providing time for the pipe to empty
        scheduler.shutdown();
        scheduler.awaitTermination(10, TimeUnit.SECONDS); //timeout error if this does not exit cleanly withing this time.
        //all the software has now stopped so now shutdown the hardware.
        hardware.shutdown();
    }

    public void shutdownDevice(int timeoutInSeconds) {
        //clean shutdown providing time for the pipe to empty
        scheduler.shutdown();
        scheduler.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS); //timeout error if this does not exit cleanly withing this time.
        //all the software has now stopped so now shutdown the hardware.
        hardware.shutdown();
    }

    public void addSubscriptionListener(String string, PubSubListener exampleController) {
        //add assert that this is only done before the graph is started
        
        // TODO Auto-generated method stub
        
    }
    
    public void addRESTListener(int id, String route, RestListener listener) {
        //add assert that this is only done before the graph is started
        

        // TODO accumulate all thse rest processor, when start is called then configure the server to take them all. 
        
    }


    
    
}
