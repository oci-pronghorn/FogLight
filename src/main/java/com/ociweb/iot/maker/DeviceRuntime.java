package com.ociweb.iot.maker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.gl.impl.schema.MessageSubscription;
import com.ociweb.gl.impl.schema.TrafficOrderSchema;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.iot.hardware.impl.edison.GroveV3EdisonImpl;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiHardwareImpl;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.iot.ReactiveListenerStageIOT;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.stage.monitor.MonitorConsoleStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DeviceRuntime extends GreenRuntime<HardwareImpl, ListenerFilterIoT>  {
 
    private static final Logger logger = LoggerFactory.getLogger(DeviceRuntime.class);
    

    private final int i2cDefaultLength = 300;
    private final int i2cDefaultMaxPayload = 16;

    private final PipeConfig<GroveRequestSchema> requestPipeConfig = new PipeConfig<GroveRequestSchema>(GroveRequestSchema.instance, defaultCommandChannelLength);
    private final PipeConfig<I2CCommandSchema> i2cPayloadPipeConfig = new PipeConfig<I2CCommandSchema>(I2CCommandSchema.instance, i2cDefaultLength,i2cDefaultMaxPayload);
    private final PipeConfig<I2CResponseSchema> reponseI2CConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, defaultCommandChannelLength, defaultCommandChannelMaxPayload);
    private final PipeConfig<GroveResponseSchema> responsePinsConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, defaultCommandChannelLength);
    private final PipeConfig<SerialDataSchema> serialInputConfig = new PipeConfig<SerialDataSchema>(SerialDataSchema.instance, defaultCommandChannelLength, defaultCommandChannelMaxPayload); 

    private static final byte piI2C = 1;
    private static final byte edI2C = 6;
    
    
    public DeviceRuntime() {
       super();     
    }


    
    public Hardware getHardware(){
    	if(this.builder==null){
    	    
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
	            this.builder = new GroveV3EdisonImpl(gm, i2cBacking);
	            logger.trace("Detected running on Edison");
	        } else {
	        	i2cBacking = HardwareImpl.getI2CBacking(piI2C);
	    	    if (null != i2cBacking) {
	    	        this.builder = new GrovePiHardwareImpl(gm, i2cBacking);
	    	        logger.trace("Detected running on Pi");
	    	    }
    	        else {
    	            this.builder = new TestHardware(gm);
    	            logger.trace("Unrecognized hardware, test mock hardware will be used");
    	        }    	        
    	    }  	    
    	    
    	}
    	return this.builder;
    }
    
    

    
    public CommandChannel newCommandChannel(int features) { 
      
    	int instance = -1;
    	
    	PipeConfigManager pcm = new PipeConfigManager();
    	pcm.addConfig(requestPipeConfig);
    	pcm.addConfig(i2cPayloadPipeConfig);
    	pcm.addConfig(defaultCommandChannelLength,0,TrafficOrderSchema.class );
    	
    	
    	return this.builder.newCommandChannel(features, instance, pcm);
    	
    }

    public CommandChannel newCommandChannel(int features, int customChannelLength) { 
       
    	int instance = -1;
    	
    	PipeConfigManager pcm = new PipeConfigManager();
    	pcm.addConfig(customChannelLength,0,GroveRequestSchema.class);
    	pcm.addConfig(customChannelLength, defaultCommandChannelMaxPayload,I2CCommandSchema.class);
    	pcm.addConfig(customChannelLength, defaultCommandChannelMaxPayload, MessagePubSub.class );
    	pcm.addConfig(customChannelLength,0,TrafficOrderSchema.class);
    	    	
        return this.builder.newCommandChannel(features, instance, pcm);
        
    }



    public ListenerFilterIoT addRotaryListener(RotaryListener listener) {
        return registerListener(listener);
    }
        
    
//TODO: where is the AnalogListener code?? was it removed??
//TODO: need to add serial listener...
    
    public ListenerFilterIoT addAnalogListener(AnalogListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilterIoT addDigitalListener(DigitalListener listener) {
        return registerListener(listener);
    }

    public ListenerFilterIoT addSerialListener(SerialListener listener) {
        return registerListener(listener);
    }
    
    public ListenerFilterIoT registerListener(Object listener) {
    	return registerListenerImpl(listener);
    }
    
    public ListenerFilterIoT addImageListener(ImageListener listener) {
    	//NOTE: this is an odd approach, this level of configuration is normally hidden on this layer.
    	//      TODO: images should have their own internal time and not hijack the application level timer.
        if (builder.getTriggerRate() < 1250) {
            throw new RuntimeException("Image listeners cannot be used with trigger rates of less than 1250 MS configured on the Hardware.");
        }

        switch (builder.getPlatformType()) {
            case GROVE_PI:
                return registerListener(new PiImageListenerBacking(listener));
            default:
                throw new UnsupportedOperationException("Image listeners are not supported for [" +
                		builder.getPlatformType() +
                                                        "] hardware");
        }
    }
        
    public ListenerFilterIoT addI2CListener(I2CListener listener) {
        return registerListenerImpl(listener);
    }
    
    
    private ListenerFilterIoT registerListenerImpl(Object listener, int ... optionalInts) {
        
    	extractPipeData(listener, optionalInts);
    	
    	/////////
    	//pre-count how many pipes will be needed so the array can be built to the right size
    	/////////
    	int pipesCount = 0;
    	if (this.builder.isListeningToI2C(listener) && this.builder.hasI2CInputs()) {
    		pipesCount++;      
        }
        if (this.builder.isListeningToPins(listener) && this.builder.hasDigitalOrAnalogInputs()) {
        	pipesCount++;
        }
    	if (this.builder.isListeningToSerial(listener) && this.builder.hasSerialInputs()) {
    		pipesCount++;      
        }
        
        pipesCount = addGreenPipesCount(listener, pipesCount);
        
        Pipe<?>[] inputPipes = new Pipe<?>[pipesCount];
    	    	

    	///////
        //Populate the inputPipes array with the required pipes
    	///////      
                
        if (this.builder.isListeningToI2C(listener) && this.builder.hasI2CInputs()) {
        	inputPipes[--pipesCount] = new Pipe<I2CResponseSchema>(reponseI2CConfig);       
        }
        if (this.builder.isListeningToPins(listener) && this.builder.hasDigitalOrAnalogInputs()) {
        	inputPipes[--pipesCount] = new Pipe<GroveResponseSchema>(responsePinsConfig);
        }
        if (this.builder.isListeningToSerial(listener) && this.builder.hasSerialInputs()) {
        	inputPipes[--pipesCount] = new Pipe<SerialDataSchema>(serialInputConfig);        
        }
        
        populateGreenPipes(listener, pipesCount, inputPipes); 
        
        /////////////////////
        //StartupListener is not driven by any response data and is called when the stage is started up. no pipe needed.
        /////////////////////
        //TimeListener, time rate signals are sent from the stages its self and therefore does not need a pipe to consume.
        /////////////////////

        ReactiveListenerStageIOT reactiveListener = builder.createReactiveListener(gm, listener, inputPipes, outputPipes);
		configureStageRate(listener,reactiveListener);
        
		int testId = -1;
		int i = inputPipes.length;
		while (--i>=0) {
			if (inputPipes[i]!=null && Pipe.isForSchema(inputPipes[i], MessageSubscription.instance)) {
				testId = inputPipes[i].id;
			}
		}
		
		assert(-1==testId || GraphManager.allPipesOfType(gm, MessageSubscription.instance)[subscriptionPipeIdx-1].id==testId) : "GraphManager has returned the pipes out of the expected order";
        return reactiveListener;
        
    }

        
    public static DeviceRuntime test(IoTSetup app) {        
        DeviceRuntime runtime = new DeviceRuntime();
        //force hardware to TestHardware regardless of where or what platform its run on.
        //this is done because this is the test() method and must behave the same everywhere.
        runtime.builder = new TestHardware(runtime.gm);
        TestHardware hardware = (TestHardware)runtime.getHardware();
        hardware.isInUnitTest = true;
        try {
        	app.declareConfiguration(runtime.builder);
            GraphManager.addDefaultNota(runtime.gm, GraphManager.SCHEDULE_RATE, runtime.builder.getDefaultSleepRateNS()); 
            
            runtime.declareBehavior(app);
            
            runtime.builder.coldSetup(); //TODO: should we add LCD init in the PI hardware code? How do we know when its used?
			
				runtime.builder.buildStages(runtime.subscriptionPipeLookup, runtime.netPipeLookup, runtime.gm);
				
				   runtime.logStageScheduleRates();
				
				   if ( runtime.builder.isTelemetryEnabled()) {	   
					   MonitorConsoleStage.attach(runtime.gm);//documents what was buit.
				   }
			   //exportGraphDotFile();      
			
			   runtime.scheduler = runtime.builder.createScheduler(runtime);
            //for test we do not call startup and wait instead for this to be done by test.
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
        return runtime;
    }
    
	public static DeviceRuntime run(IoTSetup app) {
	    DeviceRuntime runtime = new DeviceRuntime();
        try {
        	app.declareConfiguration(runtime.getHardware());
		    GraphManager.addDefaultNota(runtime.gm, GraphManager.SCHEDULE_RATE, runtime.builder.getDefaultSleepRateNS());
          
		    runtime.declareBehavior(app);
            
		    System.out.println("To exit app press Ctrl-C");
		    runtime.builder.coldSetup(); //TODO: should we add LCD init in the PI hardware code? How do we know when its used?
			
				runtime.builder.buildStages(runtime.subscriptionPipeLookup, runtime.netPipeLookup, runtime.gm);
				
				   runtime.logStageScheduleRates();
				
				   if ( runtime.builder.isTelemetryEnabled()) {	   
					   MonitorConsoleStage.attach(runtime.gm);//documents what was buit.
				   }
			   //exportGraphDotFile();      
			
			   runtime.scheduler = runtime.builder.createScheduler(runtime);
		    runtime.scheduler.startup();
		} catch (Throwable t) {
		    t.printStackTrace();
		    System.exit(-1);
		}
		return runtime;
    }

    
}
