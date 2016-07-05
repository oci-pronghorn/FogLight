package com.ociweb.iot.hardware;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Splitter;
import com.ociweb.iot.hardware.HardConnection.ConnectionType;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.TrafficCopStage;
import com.ociweb.pronghorn.iot.i2c.PureJavaI2CStage;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.route.SplitterStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class Hardware {
    
    private static final int PIN_SETUP_TIMEOUT = 3; //in seconds
    public static final int MAX_MOVING_AVERAGE_SUPPORTED = 101;
    
    private static final HardConnection[] EMPTY = new HardConnection[0];

    private final int SLEEP_RATE_NS = 20000000;

    
    public boolean configI2C;       //Humidity, LCD need I2C address so..
    public long debugI2CRateLastTime;
    
    public HardConnection[] multiBitInputs; //Rotary Encoder, and similar
    public HardConnection[] multiBitOutputs;//Some output that takes more than 1 bit
    
    public HardConnection[] digitalInputs; //Button, Motion
    public HardConnection[] digitalOutputs;//Relay Buzzer
    
    public HardConnection[] analogInputs;  //Light, UV, Moisture
    public HardConnection[] pwmOutputs;    //Servo   //(only 3, 5, 6, 9, 10, 11 when on edison)
    
    private long timeTriggerRate;
    
    public final GraphManager gm;
    
    protected final PipeConfig<TrafficReleaseSchema> releasePipesConfig          = new PipeConfig<TrafficReleaseSchema>(TrafficReleaseSchema.instance, 64);
    protected final PipeConfig<TrafficOrderSchema> orderPipesConfig          = new PipeConfig<TrafficOrderSchema>(TrafficOrderSchema.instance, 64);
    protected final PipeConfig<TrafficAckSchema> ackPipesConfig = new PipeConfig<TrafficAckSchema>(TrafficAckSchema.instance, 64);
    protected final PipeConfig<RawDataSchema> I2CToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
    protected final PipeConfig<RawDataSchema> adInToListenerConfig = new PipeConfig<RawDataSchema>(RawDataSchema.instance, 64, 1024);
    protected final PipeConfig<GroveResponseSchema> groveResponseConfig = new PipeConfig<GroveResponseSchema>(GroveResponseSchema.instance, 64, 1024);
    
    
    //TODO: ma per field with max defined here., 
    //TODO: publish with or with out ma??
    
    //only publish when the moving average changes
    
    private ReentrantLock lock = new ReentrantLock();
    
    public Hardware(GraphManager gm) {
        this(gm, false,false,EMPTY,EMPTY,EMPTY,EMPTY,EMPTY);
        
    }
    
    protected Hardware(GraphManager gm, boolean publishTime, boolean configI2C, HardConnection[] multiDigitalInput,
            HardConnection[] digitalInputs, HardConnection[] digitalOutputs, HardConnection[] pwmOutputs, HardConnection[] analogInputs) {
        
        this.configI2C = configI2C;
        this.multiBitInputs = multiDigitalInput; 
        //TODO: add multiBitOutputs and support for this new array
                
        this.digitalInputs = digitalInputs;
        this.digitalOutputs = digitalOutputs;
        this.pwmOutputs = pwmOutputs;
        this.analogInputs = analogInputs;
        this.gm = gm;
    }

    /////
    /////
    
    HardConnection[] growConnections(HardConnection[] original, HardConnection toAdd) {
        int l = original.length;
        HardConnection[] result = new HardConnection[l+1];
        System.arraycopy(original, 0, result, 0, l);
        result[l] = toAdd;
        return result;
    }
    
    public Hardware useConnectA(IODevice t, int connection) {
        return useConnectA(t,connection,-1);
    }
    
    public Hardware useConnectA(IODevice t, int connection, int customRate) {
    	HardConnection gc = (System.getProperty("os.version").toLowerCase().indexOf("edison") != -1)? new HardConnection(t,connection,ConnectionType.Direct):new HardConnection(t,connection,ConnectionType.GrovePi);
        if (t.isInput()) {
            assert(!t.isOutput());
            analogInputs = growConnections(analogInputs, gc);
        } else {
            assert(t.isOutput());
            pwmOutputs = growConnections(pwmOutputs, gc);
        }
        return this;
    }
    
    public Hardware useConnectD(IODevice t, int connection) {
        return useConnectD(t,connection,-1);
    }
    
    public Hardware useConnectD(IODevice t, int connection, int customRate) {
    	
        HardConnection gc =(System.getProperty("os.version").toLowerCase().indexOf("edison") != -1)? new HardConnection(t,connection,ConnectionType.Direct):new HardConnection(t,connection,ConnectionType.GrovePi);
        if (t.isInput()) {
            assert(!t.isOutput());
            digitalInputs = growConnections(digitalInputs, gc);
        } else {
            assert(t.isOutput());
            digitalOutputs = growConnections(digitalOutputs, gc);
        }
        return this;
    }  
    

    public Hardware useConnectDs(IODevice t, int ... connections) {

        if (t.isInput()) {
            assert(!t.isOutput());
            for(int con:connections) {
                multiBitInputs = growConnections(multiBitInputs, new HardConnection(t,con,ConnectionType.GrovePi));
            }
            
          System.out.println("connections "+Arrays.toString(connections));  
          System.out.println("Encoder here "+Arrays.toString(multiBitInputs));  
            
        } else {
            assert(t.isOutput());
            for(int con:connections) {
                multiBitOutputs = growConnections(multiBitOutputs, new HardConnection(t,con,ConnectionType.GrovePi));
            }
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
    
    
    public int maxAnalogMovingAverage() {
        return MAX_MOVING_AVERAGE_SUPPORTED;
    }

    public abstract void i2cSetClockLow();
    public abstract void i2cSetClockHigh();
    public abstract void i2cSetDataLow();
    public abstract void i2cSetDataHigh();
    public abstract int i2cReadData();
    public abstract int i2cReadClock();
    
    public abstract void i2cDataIn();
    public abstract void i2cDataOut();
    public abstract void i2cClockIn();
    public abstract void i2cClockOut();
    public abstract boolean i2cReadAck();
    public abstract boolean i2cReadClockBool();
    public abstract boolean i2cReadDataBool();
    
    public abstract void coldSetup();
    public abstract void cleanup();
    public abstract byte getI2CConnector();
        
    
    public abstract CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, 
    		Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<TrafficOrderSchema> orderPipe);

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

	

    public final void buildStages(Pipe<GroveRequestSchema>[] requestPipes, 
                                  Pipe<I2CCommandSchema>[] i2cPipes, 
                                  Pipe<GroveResponseSchema>[] responsePipes,        
                                  Pipe<TrafficOrderSchema>[] orderPipes) {
            
        
        assert(orderPipes.length == i2cPipes.length);
        assert(orderPipes.length == requestPipes.length);
        
        
        int t = orderPipes.length;
        
        Pipe<TrafficReleaseSchema>[]          masterI2CgoOut = new Pipe[t];
        Pipe<TrafficAckSchema>[]              masterI2CackIn = new Pipe[t]; 
        
        Pipe<TrafficReleaseSchema>[]          masterPINgoOut = new Pipe[t];
        Pipe<TrafficAckSchema>[]              masterPINackIn = new Pipe[t]; 
        
        while (--t>=0) {
            
            Pipe<TrafficReleaseSchema> i2cGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
            Pipe<TrafficReleaseSchema> pinGoPipe = new Pipe<TrafficReleaseSchema>(releasePipesConfig);
            Pipe<TrafficAckSchema> i2cAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
            Pipe<TrafficAckSchema> pinAckPipe = new Pipe<TrafficAckSchema>(ackPipesConfig);
        
            masterI2CgoOut[t] = i2cGoPipe;
            masterI2CackIn[t] = i2cAckPipe;
            masterPINgoOut[t] = pinGoPipe;
            masterPINackIn[t] = pinAckPipe;            
            
            Pipe<TrafficReleaseSchema>[] goOut = new Pipe[]{pinGoPipe, i2cGoPipe};
            Pipe<TrafficAckSchema>[] ackIn = new Pipe[]{pinAckPipe, i2cAckPipe};
            TrafficCopStage trafficCopStage = new TrafficCopStage(gm, orderPipes[t], ackIn, goOut);
            
        }
        
        createADOutputStage(requestPipes, masterPINgoOut, masterPINackIn);
        
        createI2COutputInputStage(i2cPipes, masterI2CgoOut, masterI2CackIn);
        
        Pipe<GroveResponseSchema> masterResponsePipe = new Pipe<GroveResponseSchema>(groveResponseConfig); 
        new SplitterStage<>(gm, masterResponsePipe, responsePipes);
        AnalogDigitalInputStage adInputStage = new AnalogDigitalInputStage(this.gm, masterResponsePipe, this); //TODO: Probably needs an ack Pipe
        
        
        
    }

    protected void createI2COutputInputStage(Pipe<I2CCommandSchema>[] i2cPipes,
            Pipe<TrafficReleaseSchema>[] masterI2CgoOut, Pipe<TrafficAckSchema>[] masterI2CackIn) {
        //NOTE: if this throws we should use the Java one here instead.
        I2CJFFIStage i2cJFFIStage = new I2CJFFIStage(gm, masterI2CgoOut, i2cPipes, masterI2CackIn, this);
        GraphManager.addNota(this.gm, GraphManager.SCHEDULE_RATE, SLEEP_RATE_NS, i2cJFFIStage);
    }


    //TODO: if the PI need to set pins directly wihout using grove this method should be overriden
    protected void createADOutputStage(Pipe<GroveRequestSchema>[] requestPipes, Pipe<TrafficReleaseSchema>[] masterPINgoOut, Pipe<TrafficAckSchema>[] masterPINackIn) {
        DirectHardwareAnalogDigitalOutputStage adOutputStage = new DirectHardwareAnalogDigitalOutputStage(gm, requestPipes, masterPINgoOut, masterPINackIn, this);
    }

	



    
}