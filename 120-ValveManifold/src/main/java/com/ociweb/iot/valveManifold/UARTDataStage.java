package com.ociweb.iot.valveManifold;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.pronghorn.iot.rs232.RS232Client;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

public class UARTDataStage extends PronghornStage{

	private RS232Client client;
	private final Pipe<RawDataSchema> output;
	private int x = 0;
	private Logger logger = LoggerFactory.getLogger(UARTDataStage.class);
	
	public static void newInstance(GraphManager gm, Pipe<RawDataSchema> output) {
		new UARTDataStage(gm, output);
		
	}
	public UARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output) {
		super(graphManager, NONE, output);
		this.output = output;
		
	}

	@Override 
	public void startup() {
        client = new RS232Client("/dev/ttyMFD1", RS232Client.B115200);
        //logger.info("started up");
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	int iteration = 0;
	@Override
	public void run() {
		
		//logger.info("enter run");
	    while (Pipe.hasRoomForWrite(output)) {
		
	    	//logger.info("has room for write");
	    	
	    	ByteBuffer[] buffers = Pipe.wrappedWritingBuffers(output);
	    	
	    	int readCount = copy(buffers);			
	    	
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
		
		
//		//System.err.println("avail "+client.getAvailableBytes());
//
//		int size = Math.min(client.getAvailableBytes(), buffers[0].remaining());
//		
//		byte[] result = client.read(size);
//		
//		
//		if (null==result || result.length==0) {
//			return 0;
//		}
//		
//		System.arraycopy(result, 0, buffers[0].array(), buffers[0].position(), result.length);
//		return result.length;
		
///////////////////////
	//ABOVE IS A PATCH UNTIL THE BINARY C CODE GETS FIXED
///////////////////////
		
		int readCount = client.readInto(buffers[0].array(), 
				buffers[0].position(), 
				buffers[0].remaining(), 
				
				buffers[1].array(), 
				buffers[1].position(),
				buffers[1].remaining());

		return readCount;
	}
}
