package com.ociweb.iot.hardware.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.impl.schema.TrafficAckSchema;
import com.ociweb.gl.impl.schema.TrafficReleaseSchema;
import com.ociweb.gl.impl.stage.AbstractTrafficOrderedStage;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.pronghorn.iot.rs232.RS232Clientable;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;



public class SerialDataWriterStage extends AbstractTrafficOrderedStage {

	private Pipe<SerialOutputSchema>[] fromCommandChannels;
	private RS232Clientable rs232Client;
	private static final Logger logger = LoggerFactory.getLogger(SerialDataWriterStage.class);
	private byte[] backing;
	private int length;
	private int mask;
	private int pos;
	private int pipeIdx = -1;

	public SerialDataWriterStage(GraphManager graphManager, 
			Pipe<SerialOutputSchema>[] ccToAdOut, //many stages requesting writes
			Pipe<TrafficReleaseSchema>[] goPipe, //traffic releases for each
			Pipe<TrafficAckSchema>[] ackPipe, 
			HardwareImpl hardware, RS232Clientable rs232Client) {
		
		super(graphManager, hardware, ccToAdOut, goPipe, ackPipe);
		this.fromCommandChannels = ccToAdOut;
		this.rs232Client = rs232Client;
		
		GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 10_000, this);
	}

	
	@Override
	public void run() {
		int activePipe = pipeIdx;
		if (activePipe>=0) {
			//never start new work until old work is finished		
			if (pumpData(activePipe)) {				
	            PipeReader.releaseReadLock(fromCommandChannels[activePipe]);
	            //only do now after we know its not blocked and was completed
	            decReleaseCount(activePipe);			
				pipeIdx = -1;
			} else {
				return;//try again later.
			}
		}
		super.run();
	}
	
	
	@Override
	protected void processMessagesForPipe(final int activePipe) {
		Pipe<SerialOutputSchema> pipe = fromCommandChannels[activePipe];
        
        while (hasReleaseCountRemaining(activePipe) 
                && isChannelUnBlocked(activePipe)
                && PipeReader.hasContentToRead(pipe)
                && isConnectionUnBlocked(PipeReader.peekInt(pipe, 1)) 
                && PipeReader.tryReadFragment(pipe) ){
  	                        
            int msgIdx = PipeReader.getMsgIdx(pipe);
           
            switch(msgIdx){
            	case SerialOutputSchema.MSG_CHUNKEDSTREAM_1:
            		
            		backing = PipeReader.readBytesBackingArray(pipe, SerialOutputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
            		length = PipeReader.readBytesLength(pipe, SerialOutputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
            		mask = PipeReader.readBytesMask(pipe, SerialOutputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
            		pos = mask & PipeReader.readBytesPosition(pipe, SerialOutputSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
            		
            		if (!pumpData(activePipe)) {
            			return;
            		}
            		
            	break;
                default:                    
                    assert(msgIdx == -1) : "unknown message "+msgIdx;
                    requestShutdown();
            }
            
            PipeReader.releaseReadLock(pipe);
            //only do now after we know its not blocked and was completed
            decReleaseCount(activePipe);
            
        }
	}


	private boolean pumpData(int activePipe) {
		Pipe<SerialOutputSchema> pipe = fromCommandChannels[activePipe];
		final int lenFromOffsetToEnd = pipe.sizeOfBlobRing - pos;           
		
		if (lenFromOffsetToEnd>=length) {
		    //simple add bytes
			
			int wroteLen = rs232Client.writeFrom(backing, pos, length);
			if (wroteLen < length) {
				//only wrote some, now what?
				//update position
				if (wroteLen>=0) {
					pos = mask&(pos+wroteLen);
					length -= wroteLen;
				}
				//
				//now store for write later
				pipeIdx = activePipe;
				return false;
			}
			
		} else {                        
		    //rolled over the end of the buffer
			int wroteLenA = rs232Client.writeFrom(backing, pos, lenFromOffsetToEnd);
			if (wroteLenA>=0) {
				pos = mask&(pos+wroteLenA);
				length -= wroteLenA;
			}
			if (wroteLenA < lenFromOffsetToEnd) {
				//
				//now store for write later
				pipeIdx = activePipe;
				return false;
			}
			
			int wroteLenB = rs232Client.writeFrom(backing, pos, length);
			if (wroteLenB < length) {
				if (wroteLenB>=0) {
					pos = mask&(pos+wroteLenB);
					length -= wroteLenB;
				}
				//
				//now store for write later
				pipeIdx = activePipe;
				return false;
			}			
		}
		pipeIdx = -1;
		return true;
	}	

}
