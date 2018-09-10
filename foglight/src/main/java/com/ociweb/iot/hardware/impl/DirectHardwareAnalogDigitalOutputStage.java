package com.ociweb.iot.hardware.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.MsgRuntime;
import com.ociweb.gl.impl.schema.TrafficAckSchema;
import com.ociweb.gl.impl.schema.TrafficReleaseSchema;
import com.ociweb.gl.impl.stage.AbstractTrafficOrderedStage;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DirectHardwareAnalogDigitalOutputStage extends AbstractTrafficOrderedStage {

	private final Pipe<GroveRequestSchema>[] fromCommandChannels;

	private static final Logger logger = LoggerFactory.getLogger(DirectHardwareAnalogDigitalOutputStage.class);


	/**
	 * Using real hardware support this stage turns on and off digital pins and sets PWM for analog out.
	 * It supports time based blocks (in ms) specific to each connection.  This way no other commands are
	 * send to that connection until the time expires.  This is across all pipes.
	 * 
	 * 
	 * @param graphManager
	 * @param ccToAdOut
	 * @param goPipe
	 * @param ackPipe
	 * @param hardware
	 */
	public DirectHardwareAnalogDigitalOutputStage(GraphManager graphManager, 
									MsgRuntime<?,?,?> runtime,
	                                Pipe<GroveRequestSchema>[] ccToAdOut,
	                                Pipe<TrafficReleaseSchema>[] goPipe,
	                                Pipe<TrafficAckSchema>[] ackPipe, HardwareImpl hardware) {
	
		super(graphManager, runtime, hardware, ccToAdOut, goPipe, ackPipe);
		this.fromCommandChannels = ccToAdOut;
		
		GraphManager.addNota(graphManager, GraphManager.ISOLATE, GraphManager.ISOLATE, this);
	}
	
	  protected void processMessagesForPipe(int activePipe) {
	      	      
	        Pipe<GroveRequestSchema> pipe = fromCommandChannels[activePipe];
	        
	        //NOTE: need to investigate how we got into this state.
	        if (hasReleaseCountRemaining(activePipe)) {
	        	if (!PipeReader.hasContentToRead(pipe)) {
	        		if (isChannelUnBlocked(activePipe)) {
	        			decReleaseCount(activePipe);
	        		}	        	
	        	}
	        }
	        
	        while (hasReleaseCountRemaining(activePipe) 
	                && isChannelUnBlocked(activePipe)
	                && PipeReader.hasContentToRead(pipe)
	                && isConnectionUnBlocked(PipeReader.peekInt(pipe, 1)) 
	                && PipeReader.tryReadFragment(pipe) ){
	  	                        
	        	
	            int msgIdx = PipeReader.getMsgIdx(pipe);
	            didWorkMonitor.published();
	            switch(msgIdx){
	                                
	                case GroveRequestSchema.MSG_DIGITALSET_110:
	                	((HardwareImpl)hardware).write(Port.DIGITALS[PipeReader.readInt(pipe,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111)], 
	                            PipeReader.readInt(pipe,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112));
	                    break;
	                                     	                    
	                case GroveRequestSchema.MSG_BLOCKCONNECTION_220:
						blockConnectionDuration(PipeReader.readInt(pipe,GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111),
								                PipeReader.readLong(pipe,GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13));
		                	
	                    break;
	                    
	                case GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221:
	                    blockConnectionUntil(PipeReader.readInt(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111),
	                                         PipeReader.readLong(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114));                         
	                    break;   
	                    
	                case GroveRequestSchema.MSG_ANALOGSET_140:
	                    
	                    ((HardwareImpl)hardware).write(Port.ANALOGS[0x03&PipeReader.readInt(pipe,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141)], 
	                            PipeReader.readInt(pipe,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142));
	                    break;
	                    
	                default:
	                    
	                    System.out.println("Wrong Message index "+msgIdx);
	                    assert(msgIdx == -1);
	                    requestShutdown();      
	                
	            }
	            PipeReader.releaseReadLock(pipe);

	            //only do now after we know its not blocked and was completed
	            decReleaseCount(activePipe);
	            
	        }

	        
	    }

	
}