package com.ociweb.pronghorn.iot;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.RestListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public abstract class ReactiveListenerStage extends PronghornStage {

    protected final Object              listener;
    protected final Pipe<?>[]           pipes;
    
    protected long                      timeTrigger;
    protected long                      timeRate;
    private final GraphManager          graphManager;           
    
    
    public ReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?> ... pipes) {
        
        super(graphManager, pipes, NONE);
        this.listener = listener;
        this.pipes = pipes;
        this.graphManager = graphManager;
        
        
    }

    public void setTimeEventSchedule(long rate) {
        
        timeRate = rate;
        long now = System.currentTimeMillis();
        if (timeTrigger <= now) {
            timeTrigger = now + timeRate;
        }
        
    }
    
    @Override
    public void startup() {
                
        //before calling any startup commands we must first ensure all commandChannels have finished starting up.
        GraphManager.spinLockUntilStageOfTypeStarted(graphManager, CommandChannel.stageClass);
                
        if (listener instanceof StartupListener) {
            ((StartupListener)listener).startup();
        }
  
    }

    @Override
    public void run() {
        
        
        //TODO: replace with linked list of processors?, NOTE each one also needs a length bound so it does not starve the rest.
        
        int p = pipes.length;
        while (--p >= 0) {
            //TODO: this solution works but smells, a "process" lambda added to the Pipe may be a better solution? Still thinking....
            Pipe<?> localPipe = pipes[p];

            if (Pipe.isForSchema(localPipe, GroveResponseSchema.instance)) {
                consumeResponseMessage(listener, (Pipe<GroveResponseSchema>) localPipe);
            } else
            if (Pipe.isForSchema(localPipe, I2CResponseSchema.instance)) { 
                
                consumeI2CMessage(listener, (Pipe<I2CResponseSchema>) localPipe);
            }
//            if (Pipe.isForSchema(localPipe, RestSomethingSchema.instance)) {
//                
//                consumeRestMessage(listener, restResponsePipes);
//            }
            else {
                //error
            }
        }
        
        processTimeEvents(listener);
        
    }


    private void processTimeEvents(Object listener) {
        //if we do have a clock schedule
        if (0 != timeRate) {
            long now = System.currentTimeMillis();
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
    

    protected abstract void consumeI2CMessage(Object listener, Pipe<I2CResponseSchema> p);
    protected abstract void consumeResponseMessage(Object listener, Pipe<GroveResponseSchema> p);
        
    
    
    
    
}
