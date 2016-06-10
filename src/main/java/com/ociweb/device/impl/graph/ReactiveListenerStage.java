package com.ociweb.device.impl.graph;

import com.ociweb.device.grove.schema.GroveResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class ReactiveListenerStage extends PronghornStage {

    private final Object listener;
    private Pipe<GroveResponseSchema>[] groveResponsePipes;
    
    protected ReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<GroveResponseSchema>[] groveResponsePipes) {
        super(graphManager, join(groveResponsePipes /*Add other pipes here from other sources*/), NONE);
        this.listener = listener;
        this.groveResponsePipes = groveResponsePipes;    
    }

    @Override
    public void run() {
        
        processGroveResponse(listener, groveResponsePipes);
        
        //if additional array sources are added then processors will go here for those pipe arrays
         
        
    }
    
    //TODO: finish this class:
    //TODO: build graph builder based on  lambdas and requested sources
    //TODO: build assert atomic checked reader.




    private void processGroveResponse(Object listener, Pipe<GroveResponseSchema>[] inputsA) {
        
        int j = inputsA.length;
        while (--j >= 0) {
            consumeResponseMessage(listener, inputsA[j]);
            
        }
        
    }

    private void consumeResponseMessage(Object listener, Pipe<GroveResponseSchema> p) {
        if (PipeReader.tryReadFragment(p)) {                
            
            int msgIdx = PipeReader.getMsgIdx(p);
            switch (msgIdx) {   //Just 4 methods??  TODO: must remove specifc field times and use the general types here as well.
                case GroveResponseSchema.MSG_TIME_10:                         
                    if (listener instanceof TimeListener) {                 
                    
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_TIME_10_FIELD_VALUE_11);
                        //TODO: for multiple clock rates we need to add a second identifier for which one this event belongs to.

                        ((TimeListener)listener).timeEvent(time);  
                    
                    }   
                break;
                case GroveResponseSchema.MSG_ANALOGSAMPLE_30:
                    if (listener instanceof AnalogListener) {
                        
                        int average = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_AVERAGE_33);
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_CONNECTOR_31);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ANALOGSAMPLE_30_FIELD_VALUE_32);
                        
                        ((AnalogListener)listener).analogEvent(connector, average, value);
                        
                    }   
                break;               
                case GroveResponseSchema.MSG_DIGITALSAMPLE_20:
                    if (listener instanceof DigitalListener) {
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_CONNECTOR_21);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_DIGITALSAMPLE_20_FIELD_VALUE_22);
                            
                        ((DigitalListener)listener).digitalEvent(connector, value);
                        
                    }   
                break; 
                case GroveResponseSchema.MSG_ENCODER_70:
                    if (listener instanceof RotaryListener) {    
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_CONNECTOR_71);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_VALUE_72);
                        int delta = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_DELTA_73);
                        int speed = PipeReader.readInt(p, GroveResponseSchema.MSG_ENCODER_70_FIELD_SPEED_74);
                        
                        ((RotaryListener)listener).rotaryEvent(connector, value, delta, speed);
                                            
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
