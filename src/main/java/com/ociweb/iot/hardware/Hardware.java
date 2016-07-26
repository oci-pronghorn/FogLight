package com.ociweb.iot.hardware;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.impl.DirectHardwareAnalogDigitalOutputStage;
//github.com/oci-pronghorn/PronghornIoT.git
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReadDeviceInputStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.i2c.I2CJFFIStage;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.iot.i2c.impl.I2CNativeLinuxBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.util.hash.IntHashTable;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.stage.scheduling.StageScheduler;
import com.ociweb.pronghorn.stage.scheduling.ThreadPerStageScheduler;

public abstract class Hardware {
    
    private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
    public static final int MAX_MOVING_AVERAGE_SUPPORTED = 101;
    
    private static final HardConnection[] EMPTY = new HardConnection[0];
    
    public boolean configI2C;       //Humidity, LCD need I2C address so..
    public long debugI2CRateLastTime;
    
    public HardConnection[] multiBitInputs; //Rotary Encoder, and similar
    public HardConnection[] multiBitOutputs;//Some output that takes more than 1 bit
    
    public HardConnection[] digitalInputs; //Button, Motion
    public HardConnection[] digitalOutputs;//Relay Buzzer
    
    public HardConnection[] analogInputs;  //Light, UV, Moisture
    public HardConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)
    
    public I2CConnection[] i2cInputs;
    public I2CConnection[] i2cOutputs;
    
    private long timeTriggerRate;
    
    public final GraphManager gm;
    
    private static final int DEFAULT_LENGTH = 16;
    private static final int DEFAULT_PAYLOAD_SIZE = 128;
    
    protected final PipeConfig<TrafficReleaseSchema> releasePipesConfig          = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, DEFAULT_LENGTH);
    protected final PipeConfig<TrafficOrderSchema> orderPipesConfig          = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, DEFAULT_LENGTH);
    protected final PipeConfig<TrafficAckSchema> ackPipesConfig = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, DEFAULT_LENGTH);
    protected final PipeConfig<GroveResponseSchema> groveResponseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);
    protected final PipeConfig<I2CResponseSchema> i2CResponseSchemaConfig = new PipeConfig<I2CResponseSchema>(I2CResponseSchema.instance, DEFAULT_LENGTH, DEFAULT_PAYLOAD_SIZE);

    public final I2CBacking i2cBacking;

    private static final Logger logger = LoggerFactory.getLogger(Hardware.class);

    
    
    //TODO: ma per field with max defined here., 
    //TODO: publish with or with out ma??
    
    //only publish when the moving average changes
    
    private ReentrantLock lock = new ReentrantLock();
    
    public Hardware(GraphManager gm, I2CBacking i2cBacking) {
        this(gm, i2cBacking, false,false,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY);
        
    }
    
    protected Hardware(GraphManager gm, I2CBacking i2cBacking, boolean publishTime, boolean configI2C, HardConnection[] multiDigitalInput,
            HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs) {
        
        this.i2cBacking = i2cBacking;
        
        this.configI2C = configI2C; //may be removed.
        
        this.multiBitInputs = multiDigitalInput; 
        //TODO: add multiBitOutputs and support for this new array
                
        this.digitalInputs = digitalInputs;
        this.digitalOutputs = digitalOutputs;
        this.pwmOutputs = pwmOutputs;
        this.analogInputs = analogInputs;
        this.gm = gm;
    }

    
    public static I2CBacking getI2CBacking(byte deviceNum) {
        try {
            return new I2CNativeLinuxBacking(deviceNum);
        } catch (Throwable t) {
            //avoid non error case that is used to detect which hardware is running.
            if (!t.getMessage().contains("Could not open")) {
                logger.error("unable to find binary bindings ", t);
            }
            return null;
        }
    }
    
    /////
    /////
    
    protected HardConnection[] growHardConnections(HardConnection[] original, HardConnection toAdd) {
        int l = original.length;
        HardConnection[] result = new HardConnection[l+1];
        System.arraycopy(original, 0, result, 0, l);
        result[l] = toAdd;
        return result;
    }
    
    protected I2CConnection[] growI2CConnections(I2CConnection[] original, I2CConnection toAdd){
    	System.out.println("Adding I2C Connection");
        if (null==original) {
            return new I2CConnection[] {toAdd};
        } else {
        	int l = original.length;
            I2CConnection[] result = new I2CConnection[l+1];
            System.arraycopy(original, 0, result, 0, l);
            result[l] = toAdd;
            return result;
        }
    }
    
    //TODO: double check new name  connectAnalog  and confirm before rename.
    public Hardware useConnectA(IODevice t, int connection) {
        return useConnectA(t,connection,-1);
    }
    
    public Hardware useConnectA(IODevice t, int connection, int customRate) {
    	HardConnection gc = new HardConnection(t,connection,customRate);
        if (t.isInput()) {
            assert(!t.isOutput());
            analogInputs = growHardConnections(analogInputs, gc);
        } else {
            assert(t.isOutput());
            pwmOutputs = growHardConnections(pwmOutputs, gc);
        }
        return this;
    }
    
    public Hardware useConnectA(IODevice t, int connection, int customRate, int customAverageMS) {
        HardConnection gc = new HardConnection(t,connection,customRate, customAverageMS);
        if (t.isInput()) {
            assert(!t.isOutput());
            analogInputs = growHardConnections(analogInputs, gc);
        } else {
            assert(t.isOutput());
            pwmOutputs = growHardConnections(pwmOutputs, gc);
        }
        return this;
    }
    
    public Hardware useConnectD(IODevice t, int connection) {
        return useConnectD(t,connection,-1);
    }
    
    public Hardware useConnectD(IODevice t, int connection, int customRate) {
    	
        HardConnection gc =new HardConnection(t,connection, customRate);
        
        if (t.isInput()) {
            assert(!t.isOutput());
            digitalInputs = growHardConnections(digitalInputs, gc);
        } else {
            assert(t.isOutput());
            digitalOutputs = growHardConnections(digitalOutputs, gc);
        }
        return this;
    }  
    
    public Hardware useConnectD(IODevice t, int connection, int customRate, int customAverageMS) {
        
        HardConnection gc =new HardConnection(t,connection, customRate, customAverageMS);
        
        if (t.isInput()) {
            assert(!t.isOutput());
            digitalInputs = growHardConnections(digitalInputs, gc);
            
            
            
        } else {
            assert(t.isOutput());
            digitalOutputs = growHardConnections(digitalOutputs, gc);
        }
        return this;
    }  
    

    public Hardware useConnectDs(IODevice t, int ... connections) {

        if (t.isInput()) {
            assert(!t.isOutput());
            for(int con:connections) {
                multiBitInputs = growHardConnections(multiBitInputs, new HardConnection(t,con));
            }
            
          System.out.println("connections "+Arrays.toString(connections));  
          System.out.println("Encoder here "+Arrays.toString(multiBitInputs));  
            
        } else {
            assert(t.isOutput());
            for(int con:connections) {
                multiBitOutputs = growHardConnections(multiBitOutputs, new HardConnection(t,con));
            }
        }
        return this;
        
    }  
    
    public Hardware useConnectI2C(IODevice t){ 
    	logger.debug("Connecting I2C Device "+t.getClass());
    	if(t.isInput()){
    		assert(!t.isOutput());
    		i2cInputs = growI2CConnections(i2cInputs, t.getI2CConnection());
    	}else if(t.isOutput()){
    		assert(!t.isInput());
    		i2cOutputs = growI2CConnections(i2cOutputs, t.getI2CConnection());
    	}
    	return this;
    }
    
    public Hardware useTriggerRate(long rateInMS) {
        timeTriggerRate = rateInMS;
        return this;
    }
    
    public long getTriggerRate() {
        return timeTriggerRate;
    }
    
    public Hardware useI2C() {
        this.configI2C = true;
        return this;
    }
    
    /////
    /////


    public void beginPinConfiguration() {
        try {
            if (!lock.tryLock(PIN_SETUP_TIMEOUT, TimeUnit.SECONDS)) {
                throw new RuntimeException("One of the stages was not able to complete startup due to pin configuration issues.");
            }
        } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
        }
    }
    
    public void endPinConfiguration() {
       lock.unlock();
    }
    
    public abstract int digitalRead(int connector); //Platform specific
    public abstract int analogRead(int connector); //Platform specific
    public abstract void digitalWrite(int connector, int value); //Platform specific
    public abstract void analogWrite(int connector, int value); //Platform specific
    public abstract ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes);
    
    public int maxAnalogMovingAverage() {
        return MAX_MOVING_AVERAGE_SUPPORTED;
    }

   
    public abstract void coldSetup();
            
    public abstract CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe);

    static final boolean debug = false;
    public void progressLog(int taskAtHand, int stepAtHand, int byteToSend) {
        
        if (debug) {
        
            long now = System.nanoTime();
    
            long duration = now-debugI2CRateLastTime;
            if (duration<PureJavaI2CStage.NS_PAUSE) {
                System.err.println("calling I2C too fast");
            }
            if (duration >= 35_000_000) {
                System.err.println("calling I2C too slow "+duration+" devices may have now timed out. next is "+taskAtHand+":"+stepAtHand);
            } else if (duration> 1_000_000 /*20_000_000*/) {
                System.err.println("warning calling I2C too slow "+duration+". next is "+taskAtHand+":"+stepAtHand);
            }
            debugI2CRateLastTime = System.nanoTime();//use new time because we must avoid recording our log message in the duration time.
        }
    }

    public void shutdown() {
        // TODO The caller would like to stop the operating system cold, need platform specific call?
    }

	

    public final void buildStages(
                                  IntHashTable subscriptionPipeLookup,
                                  Pipe<GroveResponseSchema>[] responsePipes,     //one for each listener of this type (broadcast to all)
                                  Pipe<I2CResponseSchema>[] i2cResponsePipes,    //one for each listener of this type (broadcast to all)
                                  Pipe<MessageSubscription>[] subscriptionPipes, //one for each listener of this type (subscription per pipe)
                                  
                                  Pipe<TrafficOrderSchema>[] orderPipes,   //one for each command channel 
                                  
                                  Pipe<GroveRequestSchema>[] requestPipes, //one for each command channel 
                                  Pipe<I2CCommandSchema>[] i2cPipes,       //one for each command channel 
                                  Pipe<MessagePubSub>[] messagePubSub      //one for each command channel 
                                  
            ) {
            
        
        assert(orderPipes.length == i2cPipes.length);
        assert(orderPipes.length == requestPipes.length);
      
        
        int t = orderPipes.length;
        
        Pipe<TrafficReleaseSchema>[]          masterI2CgoOut = new Pipe[t];
        Pipe<TrafficAckSchema>[]              masterI2CackIn = new Pipe[t]; 
        
        Pipe<TrafficReleaseSchema>[]          masterPINgoOut = new Pipe[t];
        Pipe<TrafficAckSchema>[]              masterPINackIn = new Pipe[t]; 
        
        Pipe<TrafficReleaseSchema>[]          masterMsggoOut = new Pipe[t];
        Pipe<TrafficAckSchema>[]              masterMsgackIn = new Pipe[t]; 
        
        
        
        while (--t>=0) {
            
            Pipe<TrafficReleaseSchema> i2cGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
            Pipe<TrafficReleaseSchema> pinGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
            Pipe<TrafficReleaseSchema> msgGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
            
            Pipe<TrafficAckSchema> i2cAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
            Pipe<TrafficAckSchema> pinAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
            Pipe<TrafficAckSchema> msgAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
        
            masterI2CgoOut[t] = i2cGoPipe;
            masterI2CackIn[t] = i2cAckPipe;
            
            masterPINgoOut[t] = pinGoPipe;
            masterPINackIn[t] = pinAckPipe;            
            
            masterMsggoOut[t] = msgGoPipe;
            masterMsgackIn[t] = msgAckPipe;  
            
            Pipe<TrafficReleaseSchema>[] goOut = new Pipe[]{pinGoPipe, i2cGoPipe, msgGoPipe};
            Pipe<TrafficAckSchema>[] ackIn = new Pipe[]{pinAckPipe, i2cAckPipe, msgAckPipe};
            long timeout = 20_000; //20 seconds
            TrafficCopStage trafficCopStage = new TrafficCopStage(gm, timeout, orderPipes[t], ackIn, goOut);
            
        }
        
        createMessagePubSubStage(subscriptionPipeLookup, messagePubSub, masterMsggoOut, masterMsgackIn, subscriptionPipes);
        
        createADOutputStage(requestPipes, masterPINgoOut, masterPINackIn);
        
        //only build and connect I2C if it is used for either in or out  
        Pipe<I2CResponseSchema> masterI2CResponsePipe = null;
        if (i2cResponsePipes.length>0) {
            masterI2CResponsePipe = new Pipe<I2CResponseSchema>(i2CResponseSchemaConfig);
            SplitterStage i2cResponseSplitter = new SplitterStage<I2CResponseSchema>(gm, masterI2CResponsePipe, i2cResponsePipes);   
        }
        if (i2cPipes.length>0 || (null!=masterI2CResponsePipe)) {
            createI2COutputInputStage(i2cPipes, masterI2CgoOut, masterI2CackIn, masterI2CResponsePipe);
        }
        
        //only build and connect gpio responses if it is used
        if (responsePipes.length>0) {
            Pipe<GroveResponseSchema> masterResponsePipe = new Pipe<GroveResponseSchema>(groveResponseConfig);
            SplitterStage responseSplitter = new SplitterStage<GroveResponseSchema>(gm, masterResponsePipe, responsePipes);      
            createADInputStage(masterResponsePipe);        
        }
        
    }

    private void createMessagePubSubStage(IntHashTable subscriptionPipeLookup,
                                          Pipe<MessagePubSub>[] messagePubSub,
                                          Pipe<TrafficReleaseSchema>[] masterMsggoOut, 
                                          Pipe<TrafficAckSchema>[] masterMsgackIn, 
                                          Pipe<MessageSubscription>[] subscriptionPipes) {
        
        new MessagePubSubStage(this.gm, subscriptionPipeLookup, this, messagePubSub, masterMsggoOut, masterMsgackIn, subscriptionPipes);
                
        
    }

    protected void createADInputStage(Pipe<GroveResponseSchema> masterResponsePipe) {
        //NOTE: rate is NOT set since stage sets and configs its own rate based on polling need.
        ReadDeviceInputStage adInputStage = new ReadDeviceInputStage(this.gm, masterResponsePipe, this);
    }

    protected void createI2COutputInputStage(Pipe<I2CCommandSchema>[] i2cPipes,
            Pipe<TrafficReleaseSchema>[] masterI2CgoOut, Pipe<TrafficAckSchema>[] masterI2CackIn, Pipe<I2CResponseSchema> masterI2CResponsePipe) {
        //NOTE: if this throws we should use the Java one here instead.
        I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, masterI2CgoOut, i2cPipes, masterI2CackIn, masterI2CResponsePipe, this);
    }

    protected void createADOutputStage(Pipe<GroveRequestSchema>[] requestPipes, Pipe<TrafficReleaseSchema>[] masterPINgoOut, Pipe<TrafficAckSchema>[] masterPINackIn) {
        DirectHardwareAnalogDigitalOutputStage adOutputStage = new DirectHardwareAnalogDigitalOutputStage(gm, requestPipes, masterPINgoOut, masterPINackIn, this);
    }

    public StageScheduler createScheduler(DeviceRuntime iotDeviceRuntime) {
        //NOTE: need to consider different schedulers in the future.
       final StageScheduler scheduler = new ThreadPerStageScheduler(gm);

       Runtime.getRuntime().addShutdownHook(new Thread() {
           public void run() {
             scheduler.shutdown();
             scheduler.awaitTermination(30, TimeUnit.MINUTES);
           }
       });
       return scheduler;
    }

    public boolean isListeningToI2C(Object listener) {
        return listener instanceof I2CListener;
    }

    public boolean isListeningToPins(Object listener) {
        return listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener;
    }

    public boolean isListeningToSubscription(Object listener) {
        return listener instanceof PubSubListener;
    }

    /**
     * access to system time.  This method is required so it can be monitored and simulated by unit tests.
     * @return
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }




    
}