package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardConnection;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.RestListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.iot.schema.MessageSubscription;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.ma.MAvgRollerLong;

public class ReactiveListenerStage extends PronghornStage {

    protected final Object              listener;
    
    protected final Pipe<?>[]           inputPipes;
    protected final Pipe<?>[]           outputPipes;
        
    protected long                      timeTrigger;
    protected long                      timeRate;   
    
    protected Hardware					hardware;
  
    private static final Logger logger = LoggerFactory.getLogger(ReactiveListenerStage.class); 
    
    private static final int MAX_SENSORS = 32;
    
    protected MAvgRollerLong[] rollingMovingAveragesAnalog;
    protected MAvgRollerLong[] rollingMovingAveragesDigital;    
    
    
    protected int[] oversampledAnalogValues;

    private static final int MAX_CONNECTIONS = 10;
    
    //for analog values returns the one with the longest run within the last n samples
    protected static final int OVERSAMPLE = 3; //  (COUNT), SAMPLE1, ... SAMPLEn
    protected static final int OVERSAMPLE_STEP = OVERSAMPLE+1;
    
    protected int[] lastDigitalValues;
    protected long[] lastDigitalTimes;
    
    protected int[] lastAnalogValues;
    protected long[] lastAnalogTimes;
    
    
    public ReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, Hardware hardware) {

        
        super(graphManager, inputPipes, outputPipes);
        this.listener = listener;

        this.inputPipes = inputPipes;
        this.outputPipes = outputPipes;       
        this.hardware = hardware;
        
        //force all commands to happen upon publish and release
        this.supportsBatchedPublish = false;
        this.supportsBatchedRelease = false;                
                
    }

    private void setupMovingAverages(MAvgRollerLong[] target, HardConnection[] con) {
        int i = con.length;
        while (--i >= 0) {
            
              int ms   = con[i].movingAverageWindowMS;
              int rate = con[i].responseMS;
              System.out.println("count "+(ms/rate)+" ms"+ms+"  rate"+rate);
              
              target[con[i].connection] = new MAvgRollerLong(ms/rate);
              System.out.println("expecting moving average on connection "+con[i].connection);
        }        
    }
    
    
    
    public void setTimeEventSchedule(long rate) {
        
        timeRate = rate;
        long now = hardware.currentTimeMillis();
        if (timeTrigger <= now) {
            timeTrigger = now + timeRate;
        }
        
    }
    
    protected int findStableReading(int tempValue, int connector) { 

        //TODO: add switch to remove this when doing fixed samples.

        int offset = updateRunLenghtOfActiveValue(tempValue, connector);
        return findMedian(offset);
    }

    
    private int findMedian(int offset) {
        assert(3 == OVERSAMPLE);//ONLY WORKS FOR 3
        
        int a = oversampledAnalogValues[offset+1];
        int b = oversampledAnalogValues[offset+2];
        int c = oversampledAnalogValues[offset+3];
        
        //TODO: what if we returned the floor?
        
        if (a>b) {
            if (b>c) {
                return b;
            } else {
                return c;
            }
        } else {
            //b > a
            if (a>c) {
                return a;
            } else {
                return c;
            }
        }
        
    }

    private int updateRunLenghtOfActiveValue(int tempValue, int connector) {

        //store this value with the oversamples
        int offset = connector*OVERSAMPLE_STEP;
        int pos =       oversampledAnalogValues[offset];                           
        
        if (--pos<=0) {
            pos = OVERSAMPLE;
        }
        oversampledAnalogValues[offset]     = pos;
        oversampledAnalogValues[offset+pos] = tempValue;
        return offset;
    }
    
    
    @Override
    public void startup() {
        //Init all the moving averages to the right size
        rollingMovingAveragesAnalog = new MAvgRollerLong[MAX_SENSORS];
        rollingMovingAveragesDigital = new MAvgRollerLong[MAX_SENSORS];
        
        System.out.println("setup analogs "+Arrays.toString(hardware.analogInputs));
        setupMovingAverages(rollingMovingAveragesAnalog, hardware.analogInputs);
        
        System.out.println("setup digitals "+Arrays.toString(hardware.digitalInputs));       
        setupMovingAverages(rollingMovingAveragesDigital, hardware.digitalInputs);
          
        lastDigitalValues = new int[MAX_CONNECTIONS];
        lastAnalogValues = new int[MAX_CONNECTIONS];
        
        lastDigitalTimes = new long[MAX_CONNECTIONS];
        lastAnalogTimes = new long[MAX_CONNECTIONS];
                    
        oversampledAnalogValues = new int[MAX_CONNECTIONS*OVERSAMPLE_STEP];
        
        //Do last so we complete all the initializations first
        if (listener instanceof StartupListener) {
        	((StartupListener)listener).startup();
        }        
    }

    @Override
    public void run() {
        
        processTimeEvents(listener);
        
        //TODO: replace with linked list of processors?, NOTE each one also needs a length bound so it does not starve the rest.
        
        int p = inputPipes.length;
        
        while (--p >= 0) {
            //TODO: this solution works but smells, a "process" lambda added to the Pipe may be a better solution? Still thinking....

            Pipe<?> localPipe = inputPipes[p];

            if (Pipe.isForSchema(localPipe, GroveResponseSchema.instance)) {
                consumeResponseMessage(listener, (Pipe<GroveResponseSchema>) localPipe);
            } else
            if (Pipe.isForSchema(localPipe, I2CResponseSchema.instance)) {                
                consumeI2CMessage(listener, (Pipe<I2CResponseSchema>) localPipe);
            } else
            if (Pipe.isForSchema(localPipe, MessageSubscription.instance)) {                
                consumePubSubMessage(listener, (Pipe<MessageSubscription>) localPipe);
            } else 
            {
                logger.error("unrecognized pipe sent to listener of type {} ", Pipe.schemaName(localPipe));
            }
        }
        
        
    }

    private StringBuilder workspace = new StringBuilder();
    private PayloadReader payloadReader;
    
    private void consumePubSubMessage(Object listener, Pipe<MessageSubscription> p) {
        while (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {
                case MessageSubscription.MSG_PUBLISH_103:
                    
                    workspace.setLength(0);
                    CharSequence topic = PipeReader.readUTF8(p, MessageSubscription.MSG_PUBLISH_103_FIELD_TOPIC_1, workspace);               
                    
                    if (null==payloadReader) {
                        payloadReader = new PayloadReader(p); 
                    }
                    
                    payloadReader.openHighLevelAPIField(MessageSubscription.MSG_PUBLISH_103_FIELD_PAYLOAD_3);

//                    if (! payloadReader.markSupported() ) {
//                        logger.warn("we need mark to be suppported for payloads in pubsub and http."); //TODO: need to implement mark, urgent.                      
//                    }
                    
                    ((PubSubListener)listener).message(topic, payloadReader);
                    
                    break;
                case -1:
                    
                    requestShutdown();
                    PipeReader.releaseReadLock(p);
                    return;
                   
                default:
                    throw new UnsupportedOperationException("Unknown id: "+msgIdx);
                
            }
            PipeReader.releaseReadLock(p);
        }
    }

    private void processTimeEvents(Object listener) {
        //if we do have a clock schedule
        if (0 != timeRate) {
            if (listener instanceof TimeListener) {
                long now = hardware.currentTimeMillis();
                if (now >= timeTrigger) {
                        ((TimeListener)listener).timeEvent(now);
                        timeTrigger += timeRate;
                }
            }
        }
    }

    private void consumeRestMessage(Object listener2, Pipe<?> p) {
        if (null!= p) {
            
            while (PipeReader.tryReadFragment(p)) {                
                
                int msgIdx = PipeReader.getMsgIdx(p);
                
                //no need to check instance of since this was registered and we have a pipe
                ((RestListener)listener).restRequest(1, null, null);
                
                //done reading message off pipe
                PipeReader.releaseReadLock(p);
            }
            
        }
    }
    
    protected void consumeI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {

        while (PipeReader.tryReadFragment(p)) {                
                    
                    int msgIdx = PipeReader.getMsgIdx(p);
                    switch (msgIdx) {   
                        case I2CResponseSchema.MSG_RESPONSE_10:
                        	processI2CMessage(listener, p);
                            break;
                        case -1:
                            
                            requestShutdown();
                            PipeReader.releaseReadLock(p);
                            return;
                           
                        default:
                            throw new UnsupportedOperationException("Unknown id: "+msgIdx);
                        
                    }
                    //done reading message off pipe
                    PipeReader.releaseReadLock(p);
        }
    }

	protected void processI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {
		if (listener instanceof I2CListener) {
			int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
			long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);
			int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);

			byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
		    
		    processI2CEvent(listener, addr, register, time, backing, position, length, mask);
		   
		}
	}

	protected void processI2CEvent(Object listener, int addr, int register, long time, byte[] backing, int position,
			int length, int mask) {
		((I2CListener)listener).i2cEvent(addr, register, time, backing, position, length, mask);
	}
	
    
    

    protected void consumeResponseMessage(Object listener, Pipe<GroveResponseSchema> p) {
        while (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {   

                case GroveResponseSchema.MSG_ANALOGSAMPLE_30:
                    if (listener instanceof AnalogListener) {
                        
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_VALUE_32);
                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_PREVDURATION_35);
                                                
                        int runningValue = findStableReading(value, connector);             
                        
                        MAvgRollerLong.roll(rollingMovingAveragesAnalog[connector], runningValue);                                                
                        
                        int mean = 0/*(int)MAvgRollerLong.mean(rollingMovingAveragesAnalog[connector])*/;
                        
                        if(value!=lastAnalogValues[connector]){   //TODO: add switch
                        	long durationMillis = 0==lastAnalogTimes[connector] ? -1 : time-lastAnalogTimes[connector];
                            ((AnalogListener)listener).analogEvent(connector, time, mean, runningValue);
                            lastAnalogValues[connector] = value;
                            lastAnalogTimes[connector] = time;
                        }
                        
                    }   
                break;               
                case GroveResponseSchema.MSG_DIGITALSAMPLE_20:
                    
                    if (listener instanceof DigitalListener) {
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_VALUE_22);
                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_PREVDURATION_25);
                                           
                        if(value!=lastDigitalValues[connector]){  //TODO: add switch   
                        	long durationMillis = 0==lastDigitalTimes[connector] ? -1 : time-lastDigitalTimes[connector];
                            ((DigitalListener)listener).digitalEvent(connector, time, durationMillis, value);
                            lastDigitalValues[connector] = value;
                            lastDigitalTimes[connector] = time;
                        }
                    }   
                break; 
                case GroveResponseSchema.MSG_ENCODER_70:
                    
                    if (listener instanceof RotaryListener) {    
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_CONNECTOR_71);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_VALUE_72);
                        int delta = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_DELTA_73);
                        int speed = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_SPEED_74);
                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_PREVDURATION_75);
                        
                        ((RotaryListener)listener).rotaryEvent(connector, time, value, delta, speed);
                                            
                    }   
                break;
                case -1:
                {    
                    requestShutdown();
                    PipeReader.releaseReadLock(p);
                    return;
                }   
                default:
                    throw new UnsupportedOperationException("Unknown id: "+msgIdx);
            }               
            
            //done reading message off pipe
            PipeReader.releaseReadLock(p);
        }
    } 

    
    
}
