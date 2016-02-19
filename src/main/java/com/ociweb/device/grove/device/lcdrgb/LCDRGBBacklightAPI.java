package com.ociweb.device.grove.device.lcdrgb;

import java.util.concurrent.locks.ReentrantLock;

import com.ociweb.device.grove.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class LCDRGBBacklightAPI extends LCDRGBBacklightAbstractStage {

    private final ReentrantLock lock = new ReentrantLock(true);
    
    
    public LCDRGBBacklightAPI(GraphManager graphManager, Pipe<I2CCommandSchema> output) {
        super(graphManager, output);
        
        GraphManager.addNota(graphManager, GraphManager.UNSCHEDULED, GraphManager.UNSCHEDULED, this); 
        
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }
    
    
    public final void blockingSetRGB(int red, int green, int blue) {
        while (!setRGB(red,green,blue)) {
            Thread.yield();
        }
    }

    public final boolean setRGB(int red, int green, int blue) {

        lock.lock();
        try {
            return requestBacklightRGB(red,green,blue);
        } finally {
            lock.unlock();
        }
    }
    
    
}
