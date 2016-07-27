package com.ociweb.iot.hardware.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.pronghorn.iot.AbstractTrafficOrderedStage;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficAckSchema;
import com.ociweb.pronghorn.iot.schema.TrafficReleaseSchema;
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
	                                Pipe<GroveRequestSchema>[] ccToAdOut,
	                                Pipe<TrafficReleaseSchema>[] goPipe,
	                                Pipe<TrafficAckSchema>[] ackPipe, Hardware hardware) {
	
		super(graphManager, hardware, ccToAdOut, goPipe, ackPipe);
		this.fromCommandChannels = ccToAdOut;
	}
	
	  protected void processMessagesForPipe(int activePipe) {
	      	      
	        Pipe<GroveRequestSchema> pipe = fromCommandChannels[activePipe];
	        
	        while (hasReleaseCountRemaining(activePipe) 
	                && !isChannelBlocked(activePipe)
	                && !connectionBlocker.isBlocked(Pipe.peekInt(pipe, 1)) 
	                && PipeReader.tryReadFragment(pipe) ){
	  	                        
	            int msgIdx = PipeReader.getMsgIdx(pipe);
	           
	            switch(msgIdx){
	                                
	                case GroveRequestSchema.MSG_DIGITALSET_110:
	                    
	                    hardware.digitalWrite(PipeReader.readInt(pipe,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111), 
	                            PipeReader.readInt(pipe,GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112));
	                    break;
	                                     	                    
	                case GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220:
	                    connectionBlocker.until(PipeReader.readInt(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_CONNECTOR_111),
	                                       hardware.currentTimeMillis() + PipeReader.readLong(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_DURATION_113));                   	                    
	                    break;
	                    
	                case GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221:
	                    connectionBlocker.until(PipeReader.readInt(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111),
	                                           PipeReader.readLong(pipe,GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114));
	                                                
	                    break;   
	                    
	                case GroveRequestSchema.MSG_ANALOGSET_140:
	                    
	                    hardware.analogWrite(PipeReader.readInt(pipe,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141), 
	                            PipeReader.readInt(pipe,GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142));
	                    break;
	                    	                    
	                case GroveRequestSchema.MSG_BLOCKCHANNELMS_22:
	                
                        blockChannelDuration(activePipe,PipeReader.readLong(pipe, GroveRequestSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13));
	                    logger.debug("CommandChannel blocked for {} millis ",PipeReader.readLong(pipe, GroveRequestSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13));
	                 
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