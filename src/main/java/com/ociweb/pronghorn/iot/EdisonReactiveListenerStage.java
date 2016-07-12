
package com.ociweb.pronghorn.iot;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class EdisonReactiveListenerStage extends ReactiveListenerStage{

    public EdisonReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {        
        super(graphManager, listener, inputPipes, outputPipes);                  
    }    

    //TODO: Should this just be the default listener stage?
    
}

