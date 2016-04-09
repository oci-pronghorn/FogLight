package com.ociweb.pronghorn.iot;

import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a group, or "stack", of linked Pronghorn Stages which comprise
 * a complete application. Every stack is provided with its own graph
 * manager and Pronghorn context, so you only need to worry about overriding
 * the {@link #start()} method and calling it!
 *
 * @author Brandon Sanders [brandon@alicorn.io]
 */
public abstract class Stack {
//Private//////////////////////////////////////////////////////////////////////

    private final GraphManager graphManager = new GraphManager();

//Protected////////////////////////////////////////////////////////////////////

    //Make it easy for users to log by providing a protected default logger.
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Adds a stage to this Stack's graph.
     *
     * @param stage Stage to add to this Stack's graph.
     */
    protected final void addStage(PronghornStage stage) {

    }

//Public///////////////////////////////////////////////////////////////////////

    /**
     * Called when this Stack is ready to be started. This method should handle
     * setting up the stack with all of its stages, pipes and so on.
     */
    public abstract void start();
}
