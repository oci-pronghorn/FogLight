
package com.ociweb.pronghorn.iot;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DefaultReactiveListenerStage extends ReactiveListenerStage{

    public DefaultReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, Hardware hardware) {        
        super(graphManager, listener, inputPipes, outputPipes, hardware);                  
    }    

    //TODO: Should this just be the default listener stage?
    
}

