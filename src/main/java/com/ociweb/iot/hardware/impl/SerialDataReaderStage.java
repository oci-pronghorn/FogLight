package com.ociweb.iot.hardware.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.rs232.RS232Client;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SerialDataReaderStage extends PronghornStage{

	private final RS232Client client;
	private final Pipe<SerialInputSchema> output;
	private Logger logger = LoggerFactory.getLogger(SerialDataReaderStage.class);
	
	public static void newInstance(GraphManager gm, Pipe<SerialInputSchema> output, RS232Client client) {
		new SerialDataReaderStage(gm, output, client);
		
	}
	public SerialDataReaderStage(GraphManager gm, Pipe<SerialInputSchema> output, RS232Client client) {
		super(gm, NONE, output);
		this.output = output;
		this.client = client;
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
		//logger.info("enter run");
	    while (Pipe.hasRoomForWrite(output)) {
		
	    	//logger.info("has room for write");
	    	
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
