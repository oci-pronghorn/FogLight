package com.ociweb.iot.maker;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.TestHardware;
import com.ociweb.iot.hardware.impl.edison.GroveV3EdisonImpl;
import com.ociweb.iot.hardware.impl.grovepi.GroveV2PiImpl;
import com.ociweb.pronghorn.iot.DefaultReactiveListenerStage;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;

public class IOTDeviceRuntime {

    //TODO: we may need a static singleton accessory for this.
    
    private static final int nsPerMS = 1_000_000;

    /*
     * Caution: in order to make good use of ProGuard we need to make an effort to avoid using static so 
     * dependencies can be traced and kept in the jar.
     *  
     */
    private static final Logger logger = LoggerFactory.getLogger(IOTDeviceRuntime.class);
    
    protected Hardware hardware;
    
    private StageScheduler scheduler;
    private final GraphManager gm;
  
    private final int defaultCommandChannelLength = 32;
    private final int defaultCommandChannelMaxPayload = 1023; //largest i2c request or pub sub payload
    
    private final PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, defaultCommandChannelLength);
    private final PipeConfig<I2CCommandSchema> i2cPayloadPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, defaultCommandChannelLength,defaultCommandChannelMaxPayload);    
    private final PipeConfig<MessagePubSub> messagePubSubConfig = new PipeConfig<MessagePubSub>(MessagePubSub.instance, defaultCommandChannelLength,defaultCommandChannelMaxPayload); 
    private final PipeConfig<TrafficOrderSchema> goPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, defaultCommandChannelLength); 
    

    private final PipeConfig<I2CResponseSchema> reponseI2CConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, 64, 1024);
    private final PipeConfig<GroveResponseSchema> responsePipeConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 64);   
    private final PipeConfig<MessageSubscription> messageSubscriptionConfig = new PipeConfig<MessageSubscription>(MessageSubscription.instance, 64, 1024);
    
    private int subscriptionPipeIdx = 0; //this implementation is dependent upon graphManager returning the pipes in the order created!
    
    private boolean isEdison = false;
    private boolean isPi = false;
    
    private int DEFAULT_SLEEP_RATE_NS = 10_000_000;   //we will only check for new work 100 times per second to keep CPU usage low.
    
    private static final byte piI2C = 1;
    private static final byte edI2C = 6;
    
    private final IntHashTable subscriptionPipeLookup = new IntHashTable(10);//NOTE: this is a maximum of 1024 listeners
    
    
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
    	    
    	    i2cBacking = Hardware.getI2CBacking(edI2C);
	        if (null != i2cBacking) {
	            this.isEdison = true;
	            this.hardware = new GroveV3EdisonImpl(gm, i2cBacking);
	            System.out.println("Detected running on Edison");
	        } else {
	        	i2cBacking = Hardware.getI2CBacking(piI2C);
	    	    if (null != i2cBacking) {
	    	        this.isPi = true;
	    	        this.hardware = new GroveV2PiImpl(gm, i2cBacking);
	    	        System.out.println("Detected running on Pi");
	    	    }
    	        else {
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
    	                                       new Pipe<MessagePubSub>(messagePubSubConfig),
    	                                       new Pipe<TrafficOrderSchema>(goPipeConfig));
    	
    }

    public CommandChannel newCommandChannel(int customChannelLength) { 
        
        return this.hardware.newCommandChannel(new Pipe<GroveRequestSchema>(new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, customChannelLength) ),
                                               new Pipe<I2CCommandSchema>(new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, customChannelLength,defaultCommandChannelMaxPayload)), 
                                               new Pipe<MessagePubSub>(new PipeConfig<MessagePubSub>(MessagePubSub.instance, customChannelLength,defaultCommandChannelMaxPayload)),
                                               new Pipe<TrafficOrderSchema>(new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, customChannelLength)));
        
    }
    
    
    public RotaryListener addRotaryListener(RotaryListener listener) {
        return (RotaryListener)registerListener(listener);
    }
    
    public StartupListener addStartupListener(StartupListener listener) {
        return (StartupListener)registerListener(listener);
    }
        
    public AnalogListener addAnalogListener(AnalogListener listener) {
        return (AnalogListener)registerListener(listener);
    }
    
    public DigitalListener addDigitalListener(DigitalListener listener) {
        return (DigitalListener)registerListener(listener);
    }
    
    public TimeListener addTimeListener(TimeListener listener) {
        return (TimeListener)registerListener(listener);
    }
        
    public I2CListener addI2CListener(I2CListener listener) {
        return (I2CListener)registerListener(listener);
    }
    
    public PubSubListener addPubSubListener(PubSubListener listener) {
        return (PubSubListener)registerListener(listener);
    }

    public Object addListener(Object listener) {
        return registerListener(listener);
    }
    
    public Object registerListener(Object listener) {
        
        List<Pipe<?>> pipesForListenerConsumption = new ArrayList<Pipe<?>>(); 
        
        
        if (this.hardware.isListeningToI2C(listener)) {
            pipesForListenerConsumption.add(new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x()));   //must double since used by splitter         
        }
        if (this.hardware.isListeningToPins(listener)) {
            pipesForListenerConsumption.add(new Pipe<GroveResponseSchema>(responsePipeConfig.grow2x()));  //must double since used by splitter
        }
        
        int subPipeIdx = -1;
        int testId = -1;
        if (this.hardware.isListeningToSubscription(listener)) {
            Pipe<MessageSubscription> subscriptionPipe = new Pipe<MessageSubscription>(messageSubscriptionConfig);
            subPipeIdx = subscriptionPipeIdx++;
            testId = subscriptionPipe.id;
            pipesForListenerConsumption.add(subscriptionPipe);
            //store this value for lookup later
            IntHashTable.setItem(subscriptionPipeLookup, System.identityHashCode(listener), subPipeIdx);
        }

        
        /////////////////////
        //StartupListener is not driven by any response data and is called when the stage is started up. no pipe needed.
        /////////////////////
        //TimeListener, time rate signals are sent from the stages its self and therefore does not need a pipe to consume.
        /////////////////////
        

        Pipe<?>[] inputPipes = pipesForListenerConsumption.toArray(new Pipe[pipesForListenerConsumption.size()]);
        Pipe<?>[] outputPipes = extractPipesUsedByListener(listener);

        if(isPi){
        	configureStageRate(listener, new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, hardware)); 
        }else{
        	configureStageRate(listener, new DefaultReactiveListenerStage(gm, listener, inputPipes, outputPipes, hardware));
        }
        
        Pipe<MessageSubscription>[] subsPipes = GraphManager.allPipesOfType(gm, MessageSubscription.instance);
        assert(-1==testId || subsPipes[subPipeIdx].id==testId) : "GraphManager has returned the pipes out of the expected order";
        
        if (testId != -1 &&  subsPipes[subPipeIdx].id==testId && subsPipes.length>3) {
            System.err.println("TESTED, NOW REMOVE THIS BLOCK **********************");
        }
        
        return listener;
        
    }

    //////////
    //only build this when assertions are on
    //////////
    private static IntHashTable cmdChannelUsageChecker;
    static {
        assert(setupForChannelAssertCheck());
    }    
    private static boolean setupForChannelAssertCheck() {
        cmdChannelUsageChecker = new IntHashTable(9);
        return true;
    }
    private boolean channelNotPreviouslyUsed(CommandChannel cmdChnl) {
        int hash = cmdChnl.hashCode();
        
        if (IntHashTable.hasItem(cmdChannelUsageChecker, hash)) {
                //this was already assigned somewhere so this is  an error
                logger.error("A CommandChannel instance can only be used exclusivly by one object or lambda. Double check where CommandChannels are passed in.", new UnsupportedOperationException());
                return false;
        } 
        //keep so this is detected later if used
        IntHashTable.setItem(cmdChannelUsageChecker, hash, 42);
        return true;
    }
    ///////////
    ///////////
    
    
    private Pipe<?>[] extractPipesUsedByListener(Object listener) {
        Pipe<?>[] outputPipes = new Pipe<?>[0];

        Class<? extends Object> c = listener.getClass();
        Field[] fields = c.getDeclaredFields();
        int f = fields.length;
        while (--f >= 0) {
            try {
                fields[f].setAccessible(true);                
                if (CommandChannel.class == fields[f].getType()) {
                    CommandChannel cmdChnl = (CommandChannel)fields[f].get(listener);                 
                    
                    assert(channelNotPreviouslyUsed(cmdChnl)) : "A CommandChannel instance can only be used exclusivly by one object or lambda. Double check where CommandChannels are passed in.";
                    cmdChnl.setListener(listener);  
                    outputPipes = PronghornStage.join(outputPipes, cmdChnl.outputPipes);
                }
            } catch (Throwable e) {
                logger.debug("unable to find CommandChannel",e);
            }
        }
        return outputPipes;
    }




    protected void configureStageRate(Object listener, ReactiveListenerStage stage) {
        //if we have a time event turn it on.
        long rate = hardware.getTriggerRate();
        if (rate>0 && listener instanceof TimeListener) {
            stage.setTimeEventSchedule(rate);
            //Since we are using the time schedule we must set the stage to be faster
            long customRate =   (rate*nsPerMS)/NonThreadScheduler.granularityMultiplier;// in ns and guanularityXfaster than clock trigger
            GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, Math.min(customRate,DEFAULT_SLEEP_RATE_NS),stage);
        }
    }

        
    public void start() {
       hardware.coldSetup();
   
       hardware.buildStages(subscriptionPipeLookup,
                            GraphManager.allPipesOfType(gm, GroveResponseSchema.instance), 
                            GraphManager.allPipesOfType(gm, I2CResponseSchema.instance),
                            GraphManager.allPipesOfType(gm, MessageSubscription.instance),
                            
                            GraphManager.allPipesOfType(gm, TrafficOrderSchema.instance), 
                            
                            GraphManager.allPipesOfType(gm, GroveRequestSchema.instance), 
                            GraphManager.allPipesOfType(gm, I2CCommandSchema.instance),
                            GraphManager.allPipesOfType(gm, MessagePubSub.instance)
               );
    
       
       //find all the instances of CommandChannel stage to startup first, note they are also unscheduled.
            
       
       //exportGraphDotFile();      
       
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
            Process result = Runtime.getRuntime().exec("dot -Tsvg -odeviceGraph.dot.svg deviceGraph.dot");
            
            if (0==result.waitFor()) {
                logger.info("Built deviceGraph.dot.png to view the runtime graph.");
            }
            
            result = Runtime.getRuntime().exec("circo -Tsvg -odeviceGraph.circo.svg deviceGraph.dot");
            
            if (0==result.waitFor()) {
                logger.info("Built deviceGraph.circo.png to view the runtime graph.");
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

    public static IOTDeviceRuntime test(IoTSetup app) {        
        IOTDeviceRuntime runtime = new IOTDeviceRuntime();
        TestHardware hardware = (TestHardware)runtime.getHardware();
        hardware.isInUnitTest = true;
        return run(app, runtime);
    }
    
	public static IOTDeviceRuntime run(IoTSetup app) {
	    IOTDeviceRuntime runtime = new IOTDeviceRuntime();
        return run(app, runtime);
    }


    private static IOTDeviceRuntime run(IoTSetup app, IOTDeviceRuntime runtime) {
        try {
            app.declareConnections(runtime.getHardware());
            app.declareBehavior(runtime);
            runtime.start();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
        return runtime;
    }
    
    
}
