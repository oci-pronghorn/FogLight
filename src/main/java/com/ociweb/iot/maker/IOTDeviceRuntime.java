package com.ociweb.iot.maker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.GroveV2PiImpl;
import com.ociweb.iot.hardware.GroveV3EdisonImpl;
import com.ociweb.iot.hardware.Hardware;
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
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;

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
    
    private List<Pipe<GroveRequestSchema>> collectedRequestPipes = new ArrayList<Pipe<GroveRequestSchema>>();
    private List<Pipe<I2CCommandSchema>> collectedI2CRequestPipes = new ArrayList<Pipe<I2CCommandSchema>>();
    private List<Pipe<TrafficOrderSchema>> collectedOrderPipes = new ArrayList<Pipe<TrafficOrderSchema>>();
    
    private List<Pipe<GroveResponseSchema>> collectedResponsePipes = new ArrayList<Pipe<GroveResponseSchema>>();
    private List<Pipe<I2CResponseSchema>> collectedI2CResponsePipes = new ArrayList<Pipe<I2CResponseSchema>>();
    
    
    private PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, 128);
    private PipeConfig<I2CCommandSchema> requestI2CPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 32, 1024); //count should never be larger than requestPipeConfig
    private PipeConfig<TrafficOrderSchema> goPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, 64, 1024); //TODO: This pipe is big. Can be made smaller?
    
    private PipeConfig<I2CCommandSchema> i2cPayloadPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, 64,1024);
    
    private PipeConfig<I2CResponseSchema> reponseI2CConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, 64, 1024);
    
    
    private PipeConfig<GroveResponseSchema> responsePipeConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 64);
    private PipeConfig<GroveResponseSchema> responsePipeConfig2x = responsePipeConfig.grow2x();
    
    private PipeConfig<TrafficOrderSchema> orderPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, 64, 1024);
    private Pipe<GroveRequestSchema> ccToAdOut = new Pipe<GroveRequestSchema>(requestPipeConfig );
    private Pipe<TrafficOrderSchema> orderPipe = new Pipe<TrafficOrderSchema>(orderPipeConfig);
    private Pipe<I2CCommandSchema> i2cPayloadPipe = new Pipe<I2CCommandSchema>(i2cPayloadPipeConfig);
    
    private boolean isEdison = false;
    private boolean isPi = false;
    
    private int DEFAULT_SLEEP_RATE_NS = 20_000_000; //we will only check for new work 50 times per second to keep CPU usage low.
    
    
    
    public IOTDeviceRuntime() {
        gm = new GraphManager();
        //by default, unless explicitly set the stages will use this sleep rate
        GraphManager.addDefaultNota(gm, GraphManager.SCHEDULE_RATE, DEFAULT_SLEEP_RATE_NS);
        
        
    }

    private static final byte piI2C = 1;
    private static final byte edI2C = 6;
    
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
    	            System.out.println("Unrecognized hardware, debug mock hardware will be used");
    	            
    	            //TODO: create mock hardware.
    	            
    	        }
    	        
    	    }
    	    
    	    
    	}
    	return this.hardware;
    }
    
    public CommandChannel newCommandChannel() { //Maybe this should all be in the same method as hardware
         
    	Pipe<GroveRequestSchema> pipe = new Pipe<GroveRequestSchema>(requestPipeConfig );
        collectedRequestPipes.add(pipe);
        Pipe<I2CCommandSchema> i2cPayloadPipe = null;
        if (hardware.configI2C) {
            i2cPayloadPipe = new Pipe<I2CCommandSchema>(i2cPayloadPipeConfig);
            collectedI2CRequestPipes.add(i2cPayloadPipe);
        } 
        Pipe<TrafficOrderSchema> orderPipe = new Pipe<TrafficOrderSchema>(goPipeConfig);
        collectedOrderPipes.add(orderPipe);
         
        return this.hardware.newCommandChannel(pipe, i2cPayloadPipe, orderPipe); //TODO: need to find out why this is in two different classes.
    	
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
        registerListener(listener);
    }
    
    public void addTimeListener(TimeListener listener) {
        registerListener(listener);
    }
        
    public void addI2CListener(I2CListener listener) {
        registerListener(listener);
    }
    
    public void registerListener(Object listener) {
        
        List<Pipe> responsePipes = new ArrayList<Pipe>(); 
        
        if (listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener) {
            Pipe<GroveResponseSchema> pipe = new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x());
            responsePipes.add(pipe);
            collectedResponsePipes.add(pipe);
        }
        
        if (listener instanceof I2CListener) {
            Pipe<I2CResponseSchema> pipe = new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x());
            responsePipes.add(pipe);
            collectedI2CResponsePipes.add(pipe);
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
        	configureStageRate(listener, new PiReactiveListenerStage(gm, listener, responsePipes.toArray(new Pipe[responsePipes.size()]))); 
        }else{
        	configureStageRate(listener, new EdisonReactiveListenerStage(gm, listener, responsePipes.toArray(new Pipe[responsePipes.size()])));
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
        
       buildGraph(); 
        
        //NOTE: need to consider different schedulers in the future.
       scheduler = new ThreadPerStageScheduler(gm);
       scheduler.startup();
       
       Runtime.getRuntime().addShutdownHook(new Thread() {
           public void run() {
             scheduler.shutdown();
             scheduler.awaitTermination(30, TimeUnit.MINUTES);
           }
       });
       
       
    }

    private void buildGraph() {

        //all the request pipes are passed into this single stage for modification of the hardware
        int s = collectedI2CRequestPipes.size();
        Pipe<I2CCommandSchema>[] i2cPipes;
        if (s>0) {
        	i2cPipes = collectedI2CRequestPipes.toArray(new Pipe[s]);    
        }else{
        	i2cPipes = null;
        }
        //all the registered listers are managed here
        
        Pipe<GroveRequestSchema>[] requestPipes    = collectedRequestPipes.toArray(new Pipe[collectedI2CRequestPipes.size()]);
        Pipe<TrafficOrderSchema>[] orderPipes      = collectedOrderPipes.toArray(new Pipe[collectedOrderPipes.size()]);
        Pipe<GroveResponseSchema>[] responsePipes  = collectedResponsePipes.toArray(new Pipe[collectedResponsePipes.size()]);
        Pipe<I2CResponseSchema>[] i2cResponsePipes = collectedI2CResponsePipes.toArray(new Pipe[collectedI2CResponsePipes.size()]);
        
        hardware.buildStages(requestPipes, i2cPipes, responsePipes, orderPipes, i2cResponsePipes);
        
//        exportGraphDotFile();
        
      
        //Do not modfy the sleep of this object it is decided inernally by the config and devices plugged in.
          
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //to produce the png we must call
        //  dot -Tpng -O deviceGraph.dot        
        Process result;
        try {
            result = Runtime.getRuntime().exec("dot -Tpng -O deviceGraph.dot");
            
            if (0==result.waitFor()) {
                System.out.println("Built deviceGraph.dot.png to view the runtime graph.");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
