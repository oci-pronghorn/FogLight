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
            switch (msgIdx) {   //Just 4 methods??
                case GroveResponseSchema.MSG_TIME_10:                        
                    if (listener instanceof TimeListener) {                 
                    
                        long time = PipeReader.readLong(p, GroveResponseSchema.MSG_TIME_10_FIELD_VALUE_11);
                        //TODO: for multiple clock rates we need to add a second identifier for which one this event belongs to.

                        ((TimeListener)listener).timeEvent(time);  
                    
                    }   
                break;
                case GroveResponseSchema.MSG_UV_20:
                    if (listener instanceof AnalogListener) {
                        
                        int average = PipeReader.readInt(p, GroveResponseSchema.MSG_UV_20_FIELD_AVERAGE_23);
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_UV_20_FIELD_CONNECTOR_21);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_UV_20_FIELD_VALUE_22);
                        
                        ((AnalogListener)listener).analogEvent(msgIdx, connector, average, value);
                        
                    }   
                break;
                case GroveResponseSchema.MSG_LIGHT_30:
                    if (listener instanceof AnalogListener) {   
                        
                        int average = PipeReader.readInt(p, GroveResponseSchema.MSG_LIGHT_30_FIELD_AVERAGE_33);
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_LIGHT_30_FIELD_CONNECTOR_31);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_LIGHT_30_FIELD_VALUE_32);
                                                
                        ((AnalogListener)listener).analogEvent(msgIdx, connector, average, value);
                        
                    }   
                break;
                case GroveResponseSchema.MSG_MOISTURE_40:
                    if (listener instanceof AnalogListener) {
                        int average = PipeReader.readInt(p, GroveResponseSchema.MSG_MOISTURE_40_FIELD_AVERAGE_43);
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_MOISTURE_40_FIELD_CONNECTOR_41);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_MOISTURE_40_FIELD_VALUE_42);
                     
                        ((AnalogListener)listener).analogEvent(msgIdx, connector, average, value);
                                                
                    }   
                break;
                case GroveResponseSchema.MSG_BUTTON_50:
                    if (listener instanceof DigitalListener) {
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_BUTTON_50_FIELD_CONNECTOR_51);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_BUTTON_50_FIELD_VALUE_52);
                            
                        ((DigitalListener)listener).digitalEvent(msgIdx, connector, value);
                        
                    }   
                break;
                case GroveResponseSchema.MSG_MOTION_60:
                    if (listener instanceof DigitalListener) {   
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_MOTION_60_FIELD_CONNECTOR_61);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_MOTION_60_FIELD_VALUE_62);
                     
                        ((DigitalListener)listener).digitalEvent(msgIdx, connector, value);
                    }   
                break;
                case GroveResponseSchema.MSG_ROTARY_70:
                    if (listener instanceof RotaryListener) {    
                        int connector = PipeReader.readInt(p, GroveResponseSchema.MSG_ROTARY_70_FIELD_CONNECTOR_71);
                        int value = PipeReader.readInt(p, GroveResponseSchema.MSG_ROTARY_70_FIELD_VALUE_72);
                        int delta = PipeReader.readInt(p, GroveResponseSchema.MSG_ROTARY_70_FIELD_DELTA_73);
                        int speed = PipeReader.readInt(p, GroveResponseSchema.MSG_ROTARY_70_FIELD_SPEED_74);
                        
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
