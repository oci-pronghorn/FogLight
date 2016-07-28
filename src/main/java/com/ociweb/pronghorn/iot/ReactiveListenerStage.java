package com.ociweb.pronghorn.iot;

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
              target[con[i].connection] = new MAvgRollerLong(ms/rate);
  
        }        
    }
    
    
    
    public void setTimeEventSchedule(long rate) {
        
        timeRate = rate;
        long now = hardware.currentTimeMillis();
        if (timeTrigger <= now) {
            timeTrigger = now + timeRate;
        }
        
    }
    
    @Override
    public void startup() {
 
        if (listener instanceof StartupListener) {
            ((StartupListener)listener).startup();
        }
                
        //Init all the moving averages to the right size
        rollingMovingAveragesAnalog = new MAvgRollerLong[MAX_SENSORS];
        rollingMovingAveragesDigital = new MAvgRollerLong[MAX_SENSORS];
        
        setupMovingAverages(rollingMovingAveragesAnalog, hardware.analogInputs);
        setupMovingAverages(rollingMovingAveragesDigital, hardware.digitalInputs);
  
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
            long now = hardware.currentTimeMillis();
            if (now >= timeTrigger) {
                if (listener instanceof TimeListener) {
                    ((TimeListener)listener).timeEvent(now);
                    timeTrigger = now + timeRate;
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
                            if (listener instanceof I2CListener) {
                                
                                int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
                                int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
                                int time = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);
                                
                                byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
                                int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
                                int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
                                int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
                                
                                ((I2CListener)listener).i2cEvent(addr, register, time, backing, position, length, mask);
                               
                            }
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

    protected void consumeResponseMessage(Object listener, Pipe<GroveResponseSchema> p) {
        while (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {   

                case GroveResponseSchema.MSG_ANALOGSAMPLE_30:
                    if (listener instanceof AnalogListener) {
                        
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_VALUE_32);
                        //TODO: remove average.
                        int average = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_AVERAGE_33);

                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_PREVDURATION_35);
                                                
                        //MAvgRollerLong.roll(rollingMovingAveragesAnalog[connector], value);                                                
                        int mean = 0/*(int)MAvgRollerLong.mean(rollingMovingAveragesAnalog[connector])*/;
						((AnalogListener)listener).analogEvent(connector, time, mean, value);
                        
                    }   
                break;               
                case GroveResponseSchema.MSG_DIGITALSAMPLE_20:
                    
                    if (listener instanceof DigitalListener) {
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21);
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_TIME_11);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_VALUE_22);
                        long duration = PipeReader.readLong(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_PREVDURATION_25);
                                           
                        
                        ((DigitalListener)listener).digitalEvent(connector, time, value);
                        
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
