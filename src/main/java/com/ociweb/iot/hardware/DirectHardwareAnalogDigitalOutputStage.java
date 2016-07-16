package com.ociweb.iot.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
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
	
	  protected void processMessagesForPipe(int a) {
	      
	      
	        while (hasReleaseCountRemaining(a) 
	                && (Pipe.hasContentToRead(fromCommandChannels[activePipe]) && !connectionBlocker.isBlocked(Pipe.peekInt(fromCommandChannels[activePipe], 1)) )
	                && PipeReader.tryReadFragment(fromCommandChannels[activePipe]) ){
	  
	            long now = System.currentTimeMillis();
	            connectionBlocker.releaseBlocks(now);
	                        
	            
	            assert(PipeReader.isNewMessage(fromCommandChannels [activePipe])) : "This test should only have one simple message made up of one fragment";
	            int msgIdx = PipeReader.getMsgIdx(fromCommandChannels [activePipe]);
	           
	            switch(msgIdx){
	                                
	                case GroveRequestSchema.MSG_DIGITALSET_110:
	                    
	                    hardware.digitalWrite(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111), 
	                            PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112));
	                    break;
	                    
	                case GroveRequestSchema.MSG_DIGITALSETANDBLOCK_210:
	                    
	                    hardware.digitalWrite(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSETANDBLOCK_210_FIELD_CONNECTOR_111), 
	                            PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSETANDBLOCK_210_FIELD_VALUE_112));
	                    
	                    connectionBlocker.until(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSETANDBLOCK_210_FIELD_CONNECTOR_111),
	                            now + PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_DIGITALSETANDBLOCK_210_FIELD_DURATION_113));
	                    
	                    break;                      
	                    
	                case GroveRequestSchema.MSG_BLOCK_220:
	                    System.out.println("Read the MSG_BLOCK from the grove request pipe");
	                	System.out.println("the  connector is: "+PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_BLOCK_220_FIELD_CONNECTOR_111));
	                	System.out.println("the duration is : "+(long)PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_BLOCK_220_FIELD_DURATION_113));
	                    connectionBlocker.until(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_BLOCK_220_FIELD_CONNECTOR_111),
	                            now + PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_BLOCK_220_FIELD_DURATION_113));
	                    
	                    
	                    break;
	                    
	                case GroveRequestSchema.MSG_ANALOGSET_140:
	                    System.out.println("Reading the Analog message from the pipe");
	                    hardware.analogWrite(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141), 
	                            PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142));
	                    break;
	                    
	                case GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240:
	                    
	                    hardware.analogWrite(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_CONNECTOR_141), 
	                            PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_VALUE_142));
	                    
	                    connectionBlocker.until(PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_CONNECTOR_141),
	                            now + PipeReader.readInt(fromCommandChannels [activePipe],GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_DURATION_113));
	                    
	                    break;	                    
	                    
	                default:
	                    
	                    System.out.println("Wrong Message index "+msgIdx);
	                    assert(msgIdx == -1);
	                    requestShutdown();      
	                
	            }
	            PipeReader.releaseReadLock(fromCommandChannels [activePipe]);
	            	            
	            //only do now after we know its not blocked and was completed
	            decReleaseCount(a);
	            
	        }
	    }

	
}