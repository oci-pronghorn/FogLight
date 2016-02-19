package com.ociweb.device.grove.device.lcdrgb;

import java.util.concurrent.locks.ReentrantLock;

import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class LCDRGBContentAPI extends LCDRGBContentAbstractStage {

    private final ReentrantLock lock = new ReentrantLock(true);
    
    
    public LCDRGBContentAPI(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, output);
        
        GraphManager.addNota(graphManager, GraphManager.UNSCHEDULED, GraphManager.UNSCHEDULED, this); 
        
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }
    
    
    public final void blockingSetText(CharSequence text) {
        while (!setText(text)) {
            Thread.yield();
        }
    }

    public final boolean setText(CharSequence text) {

        lock.lock();
        try {
            return requestText(text);  
        } finally {
            lock.unlock();
        }
    }
    
    
}
