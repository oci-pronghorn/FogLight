package com.ociweb.iot.maker;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.edison.GroveV3EdisonImpl;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiHardwareImpl;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.NetResponseSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;

public class DeviceRuntime {
 
    //TODO: we may need a static singleton accessory for this.
    
    private static final int nsPerMS = 1_000_000;

    /*
     * Caution: in order to make good use of ProGuard we need to make an effort to avoid using static so 
     * dependencies can be traced and kept in the jar.
     *  
     */
    private static final Logger logger = LoggerFactory.getLogger(DeviceRuntime.class);
    
    protected HardwareImpl hardware;
    
    private StageScheduler scheduler;
    private final GraphManager gm;
  
    private final int defaultCommandChannelLength = 16;
    private final int defaultCommandChannelMaxPayload = 256; //largest i2c request or pub sub payload
    
    private final int i2cDefaultLength = 300;
    private final int i2cDefaultMaxPayload = 16;
    
    /////////////
    //Pipes for requesting GPIO operations both Analog and Digital
    private final PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, defaultCommandChannelLength);
    
    ////////////
    ///Pipes for writing to I2C bus
    private final PipeConfig<I2CCommandSchema> i2cPayloadPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, i2cDefaultLength,i2cDefaultMaxPayload);    
    
    ////////////
    ///Pipes for subscribing to and sending messages this is MQTT, StateChanges and many others
    private final PipeConfig<MessagePubSub> messagePubSubConfig = new PipeConfig<MessagePubSub>(MessagePubSub.instance, defaultCommandChannelLength,defaultCommandChannelMaxPayload); 
   
    ////////////
    //Each of the above pipes are paired with TrafficOrder pipe to group commands togetehr in atomic groups and to enforce order across the pipes.
    private final PipeConfig<TrafficOrderSchema> goPipeConfig = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, defaultCommandChannelLength); 
    
    ///////////
    ///Pipes containing response data from the I2C bus, this primarily is polled at a fixed rate on startup
    private final PipeConfig<I2CResponseSchema> reponseI2CConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, defaultCommandChannelLength, defaultCommandChannelMaxPayload);
    
    //////////
    //Pipes containing response data from the GPIO pins both digital and analog, this is primarily polled at a fixed rate on startup.
    private final PipeConfig<GroveResponseSchema> responsePinsConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, defaultCommandChannelLength);      
    
    //////////
    //Pipes containing response data from HTTP requests.
    private final PipeConfig<NetResponseSchema> responseNetConfig = new PipeConfig<NetResponseSchema>(NetResponseSchema.instance, defaultCommandChannelLength);   
    
    
    
    /////////
    //Pipes for receiving messages, this includes MQTT, State and many others
    private final PipeConfig<MessageSubscription> messageSubscriptionConfig = new PipeConfig<MessageSubscription>(MessageSubscription.instance, defaultCommandChannelLength, defaultCommandChannelMaxPayload);
    
        
    
    private int subscriptionPipeIdx = 0; //this implementation is dependent upon graphManager returning the pipes in the order created!
    
    
    private int defaultSleepRateNS = 10_000_000;   //we will only check for new work 100 times per second to keep CPU usage low.
    
    private static final byte piI2C = 1;
    private static final byte edI2C = 6;
    
    private final IntHashTable subscriptionPipeLookup = new IntHashTable(10);//NOTE: this is a maximum of 1024 listeners
    
    
    public DeviceRuntime() {
        gm = new GraphManager();
     
    }

    
    public HardwareImpl getHardware(){
    	if(this.hardware==null){
    	    
            ///////////////
            //setup system for binary binding in case Zulu is found on Arm
            //must populate os.arch as "arm" instead of "aarch32" or "aarch64" in that case, JIFFI is dependent on this value.
    	    if (System.getProperty("os.arch", "unknown").contains("aarch")) {
    	        System.setProperty("os.arch", "arm"); //TODO: investigate if this a bug against jiffi or zulu and inform them 
    	    }
    	    
    	    
    	    ////////////////////////
    	    //The best way to detect the pi or edison is to first check for the expected matching i2c implmentation
    	    ///////////////////////
    	    
    	    I2CBacking i2cBacking = null;

    	    i2cBacking = HardwareImpl.getI2CBacking(edI2C);
	        if (null != i2cBacking) {
	            this.hardware = new GroveV3EdisonImpl(gm, i2cBacking);
	            System.out.println("Detected running on Edison");
	        } else {
	        	i2cBacking = HardwareImpl.getI2CBacking(piI2C);
	    	    if (null != i2cBacking) {
	    	        this.hardware = new GrovePiHardwareImpl(gm, i2cBacking);
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
    
    
    private Pipe<MessagePubSub> newPubSubPipe(PipeConfig<MessagePubSub> config) {
    	return new Pipe<MessagePubSub>(config) {
			@SuppressWarnings("unchecked")
			@Override
			protected DataOutputBlobWriter<MessagePubSub> createNewBlobWriter() {
				return new PayloadWriter(this);
			}
    		
    	};
    }
    
    public CommandChannel newCommandChannel() { 
      
    	return this.hardware.newCommandChannel(new Pipe<GroveRequestSchema>(requestPipeConfig ),
    	                                       new Pipe<I2CCommandSchema>(i2cPayloadPipeConfig), 
    	                                       newPubSubPipe(messagePubSubConfig),
    	                                       null, //TODO: add Pipe<NetRequestSchema> httpRequest,
    	                                       new Pipe<TrafficOrderSchema>(goPipeConfig));
    	
    }

    public CommandChannel newCommandChannel(int customChannelLength) { 
       
        return this.hardware.newCommandChannel(new Pipe<GroveRequestSchema>(new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, customChannelLength) ),
                                               new Pipe<I2CCommandSchema>(new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, customChannelLength,defaultCommandChannelMaxPayload)), 
                                               newPubSubPipe(new PipeConfig<MessagePubSub>(MessagePubSub.instance, customChannelLength,defaultCommandChannelMaxPayload)),
                                               null, //TODO: add Pipe<NetRequestSchema> httpRequest,
                                               new Pipe<TrafficOrderSchema>(new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, customChannelLength)));
        
    }
    
    
    public ListenerFilter addRotaryListener(RotaryListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilter addStartupListener(StartupListener listener) {
        return registerListener(listener);
    }
        
    public ListenerFilter addAnalogListener(AnalogListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilter addDigitalListener(DigitalListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilter addTimeListener(TimeListener listener) {
        return registerListener(listener);
    }
        
    public ListenerFilter addI2CListener(I2CListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilter addPubSubListener(PubSubListener listener) {
        return registerListener(listener);
    }

    public <E extends Enum<E>> StateChangeListener<E> addStateChangeListener(StateChangeListener<E> listener) {
        return (StateChangeListener<E>)registerListener(listener);
    }
    
    public ListenerFilter addListener(Object listener) {
        return registerListener(listener);
    }
    
    public ListenerFilter registerListener(Object listener) {
        
    	/////////
    	//pre-count how many pipes will be needed so the array can be built to the right size
    	/////////
    	int pipesCount = 0;
    	if (this.hardware.isListeningToI2C(listener) && this.hardware.hasI2CInputs()) {
    		pipesCount++;      
        }
        if (this.hardware.isListeningToPins(listener) && this.hardware.hasDigitalOrAnalogInputs()) {
        	pipesCount++;
        }
        if (this.hardware.isListeningToHTTPResponse(listener)) {
        	pipesCount++;
        }
        if (this.hardware.isListeningToSubscription(listener)) {
        	pipesCount++;
        }
        Pipe<?>[] inputPipes = new Pipe<?>[pipesCount];
    	 	
    	
    	///////
        //Populate the inputPipes array with the required pipes
    	///////      
        
        
        if (this.hardware.isListeningToI2C(listener) && this.hardware.hasI2CInputs()) {
        	inputPipes[--pipesCount] = (new Pipe<I2CResponseSchema>(reponseI2CConfig.grow2x()));   //must double since used by splitter         
        }
        if (this.hardware.isListeningToPins(listener) && this.hardware.hasDigitalOrAnalogInputs()) {
        	inputPipes[--pipesCount] = (new Pipe<GroveResponseSchema>(responsePinsConfig.grow2x()));  //must double since used by splitter
        }
        if (this.hardware.isListeningToHTTPResponse(listener)) {        	
        	inputPipes[--pipesCount] = new Pipe<NetResponseSchema>(responseNetConfig) {
				@SuppressWarnings("unchecked")
				@Override
				protected DataInputBlobReader<NetResponseSchema> createNewBlobReader() {
					return new PayloadReader(this);
				}
            };        	
        }
        
        int subPipeIdx = -1;
        int testId = -1;
        if (this.hardware.isListeningToSubscription(listener)) {
            Pipe<MessageSubscription> subscriptionPipe = new Pipe<MessageSubscription>(messageSubscriptionConfig) {
				@SuppressWarnings("unchecked")
				@Override
				protected DataInputBlobReader<MessageSubscription> createNewBlobReader() {
					return new PayloadReader(this);
				}
            };
            subPipeIdx = subscriptionPipeIdx++;
            testId = subscriptionPipe.id;
            inputPipes[--pipesCount]=(subscriptionPipe);
            //store this value for lookup later
            IntHashTable.setItem(subscriptionPipeLookup, System.identityHashCode(listener), subPipeIdx);
        }

        
        /////////////////////
        //StartupListener is not driven by any response data and is called when the stage is started up. no pipe needed.
        /////////////////////
        //TimeListener, time rate signals are sent from the stages its self and therefore does not need a pipe to consume.
        /////////////////////
        
        Pipe<?>[] outputPipes = extractPipesUsedByListener(listener);

        ReactiveListenerStage reactiveListener = hardware.createReactiveListener(gm, listener, inputPipes, outputPipes);
		configureStageRate(listener,reactiveListener);
        
        Pipe<MessageSubscription>[] subsPipes = GraphManager.allPipesOfType(gm, MessageSubscription.instance);
        assert(-1==testId || subsPipes[subPipeIdx].id==testId) : "GraphManager has returned the pipes out of the expected order";
        
        return reactiveListener;
        
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
        //keep so this is detected later if use;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
        
        
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
            stage.setTimeEventSchedule(rate, hardware.getTriggerStart());
            //Since we are using the time schedule we must set the stage to be faster
            long customRate =   (rate*nsPerMS)/NonThreadScheduler.granularityMultiplier;// in ns and guanularityXfaster than clock trigger
            long appliedRate = Math.min(customRate,defaultSleepRateNS);
            GraphManager.addNota(gm, GraphManager.SCHEDULE_RATE, appliedRate, stage);
        }
    }

        
    private void start() {
       hardware.coldSetup(); //TODO: should we add LCD init in the PI hardware code? How do we know when its used?

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
            
       
       logStageScheduleRates();
       
       
       //exportGraphDotFile();      
       
       scheduler = hardware.createScheduler(this);       

    }


    private void logStageScheduleRates() {
        int totalStages = GraphManager.countStages(gm);
           for(int i=1;i<=totalStages;i++) {
               PronghornStage s = GraphManager.getStage(gm, i);
               if (null != s) {
                   
                   Object rate = GraphManager.getNota(gm, i, GraphManager.SCHEDULE_RATE, null);
                   if (null == rate) {
                       logger.debug("{} is running without breaks",s);
                   } else  {
                       logger.debug("{} is running at rate of {}",s,rate);
                   }
               }
               
           }
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

    public static DeviceRuntime test(IoTSetup app) {        
        DeviceRuntime runtime = new DeviceRuntime();
        //force hardware to TestHardware regardless of where or what platform its run on.
        //this is done because this is the test() method and must behave the same everywhere.
        runtime.hardware = new TestHardware(runtime.gm);
        TestHardware hardware = (TestHardware)runtime.getHardware();
        hardware.isInUnitTest = true;
        try {
            app.declareConnections(runtime.getHardware());
            establishDefaultRate(runtime); 
            app.declareBehavior(runtime);
            runtime.start();
            //for test we do not call startup and wait instead for this to be done by test.
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
        return runtime;
    }
    
	public static DeviceRuntime run(IoTSetup app) {
	    DeviceRuntime runtime = new DeviceRuntime();
        return run(app, runtime);
    }


    private static DeviceRuntime run(IoTSetup app, DeviceRuntime runtime) {
        try {
            app.declareConnections(runtime.getHardware());
            establishDefaultRate(runtime);
            app.declareBehavior(runtime);
            System.out.println("To exit app press Ctrl-C");
            runtime.start();
            runtime.scheduler.startup();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
        return runtime;
    }


	private static void establishDefaultRate(DeviceRuntime runtime) {
		
		//NOTE: this must be a prime factor of all things going on !!
		
		//TODO: based on the connected twigs we must choose an appropriate default, may need schedule!!!
		//runtime.defaultSleepRateNS = 100_000;
		            
		//by default, unless explicitly set the stages will use this sleep rate
		GraphManager.addDefaultNota(runtime.gm, GraphManager.SCHEDULE_RATE, runtime.defaultSleepRateNS);
	}
    
    
}
