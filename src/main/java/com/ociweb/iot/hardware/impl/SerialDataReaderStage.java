package com.ociweb.iot.hardware.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.rs232.RS232Clientable;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SerialDataReaderStage extends PronghornStage{

	private final RS232Clientable client;
	private final Pipe<SerialInputSchema> output;
	private Logger logger = LoggerFactory.getLogger(SerialDataReaderStage.class);
	
	public static SerialDataReaderStage newInstance(GraphManager gm, Pipe<SerialInputSchema> output, RS232Clientable client) {
		return new SerialDataReaderStage(gm, output, client);
		
	}
	public SerialDataReaderStage(GraphManager gm, Pipe<SerialInputSchema> output, RS232Clientable client) {
		super(gm, NONE, output);
		this.output = output;
		this.client = client;
	}
	
	@Override
	public void shutdown() {
		if (Pipe.hasRoomForWrite(output, Pipe.EOF_SIZE)) {
			Pipe.publishEOF(output);
		}
		//if not the system is already shutting down so this is not an issue
	}
	
	@Override
	public void run() {
		
		int maxIter = 1000;//must allow for shutdown checks periodically.
	    while (--maxIter>0 && Pipe.hasRoomForWrite(output)) {
		
	    	int readCount = copy(Pipe.wrappedWritingBuffers(output));			
	    	
	    	//logger.info("found bytes on UART of count {} on iteration{}  ",readCount, iteration);
	    		    	
	    	if (readCount<=0) {
	    		 
	    		//cancel this write
	    		Pipe.unstoreBlobWorkingHeadPosition(output);
	    		return;//come back later when we have some data to read.
	    	} else {
	    	   // logger.info("found bytes on UART of count {} ",readCount);
	    		
	    		//publish this write	    	
				final int size = Pipe.addMsgIdx(output, RawDataSchema.MSG_CHUNKEDSTREAM_1);
				Pipe.moveBlobPointerAndRecordPosAndLength((int)readCount, output);
				Pipe.confirmLowLevelWrite(output, size);
				Pipe.publishWrites(output);
				
				//logger.info("relayed from uart {} bytes",size);
	    	}
	    }
		
	}

	private int copy(ByteBuffer[] buffers) {
		
		int readCount = client.readInto(buffers[0].array(), 
				buffers[0].position(), 
				buffers[0].remaining(), 
				
				buffers[1].array(), 
				buffers[1].position(),
				buffers[1].remaining());

		return readCount;
	}
}
