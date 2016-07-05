package com.ociweb.pronghorn;

import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

//TODO: this class and its schemas will be moved to the general Pronghorn project, 
//TODO: once moved over to pronghorn add unit tests arround it.


/**
 * Supports a single primary input pipe that defines which  output pipes should be processed and in which order.
 * 
 * @author Nathan Tippy
 *
 */
public class TrafficCopStage extends PronghornStage {
    
    private Pipe<TrafficOrderSchema> primaryIn; 
    private Pipe<TrafficAckSchema>[] ackIn;
    private Pipe<TrafficReleaseSchema>[] goOut;
    private int ackExpectedOn = -1;   
    
    public TrafficCopStage(GraphManager graphManager, Pipe<TrafficOrderSchema> primaryIn, Pipe<TrafficAckSchema>[] ackIn,  Pipe<TrafficReleaseSchema>[] goOut) {
    	super(graphManager, join(ackIn, primaryIn), goOut);
    	
    
        this.primaryIn = primaryIn;
        this.ackIn = ackIn;
        this.goOut = goOut;
        
    }    
    
    @Override
    public void run() {
        do {
        	
            ////////////////////////////////////////////////
            //check first if we are waiting for an ack back
            ////////////////////////////////////////////////
            if (ackExpectedOn>=0) {            
                if (!PipeReader.tryReadFragment(ackIn[ackExpectedOn])) {
                	
                    return;//we are still waiting for requested operation to complete
                } else {
                    PipeReader.releasePendingReadLock(ackIn[ackExpectedOn]);
                    ackExpectedOn = -1;//clear value we are no longer waiting
                }
            }
            ////////////////////////////////////////////////////////
            //check second for new stages to release from primaryIn
            ////////////////////////////////////////////////////////
            if (!PipeReader.tryReadFragment(primaryIn)) {
                return;//there is nothing new to send
            } else { 
            	
                if (TrafficOrderSchema.MSG_GO_10 == PipeReader.getMsgIdx(primaryIn)) {
                    //read which pipe should be used, set it as expecting to send an ack and get the Pipe object
                    Pipe<TrafficReleaseSchema> releasePipe = goOut[ackExpectedOn = PipeReader.readInt(primaryIn, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11)];
                    //send the release count message
                    if (PipeWriter.tryWriteFragment(releasePipe, TrafficReleaseSchema.MSG_RELEASE_20)) {                
                        PipeWriter.writeInt(releasePipe, TrafficReleaseSchema.MSG_RELEASE_20_FIELD_COUNT_22, PipeReader.readInt(primaryIn, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12));
                        System.out.println("Cop writing to JFFI");
                        PipeWriter.publishWrites(releasePipe);
                    } else {
                        throw new UnsupportedOperationException("The outgoing pipe "+releasePipe+" must be bigger to hold release request");
                    }
                    //release the read lock now that we published the write
                    PipeReader.releaseReadLock(primaryIn);  
                } else {
                    //this may be shutting down or an unsupported message
                    assert(-1 == PipeReader.getMsgIdx(primaryIn)) : "Expected end of stream however got unsupported message: "+PipeReader.getMsgIdx(primaryIn);
                    requestShutdown();
                    PipeReader.releaseReadLock(primaryIn);  
                    return;//reached end of stream
                }
            }
        } while(true);
    }

}
