package com.ociweb.iot.valveManifold;

import java.nio.ByteBuffer;

import com.ociweb.pronghorn.iot.rs232.RS232Client;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class UARTDataStage extends PronghornStage{

	private RS232Client client;
	private final Pipe<RawDataSchema> output;
	private int x = 0;
	
	public static void instance(GraphManager gm, Pipe<RawDataSchema> output) {
		new UARTDataStage(gm, output);
		
	}
	public UARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output) {
		super(graphManager, NONE, output);
		this.output = output;
		
		GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 1_000_000_000, this); //testing once per second.
	}

	@Override 
	public void startup() {
        client = new RS232Client("/dev/ttyMFD1", RS232Client.B115200);
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
	    while (Pipe.hasRoomForWrite(output)) {
		
	    	ByteBuffer[] buffers = Pipe.wrappedWritingBuffers(output);
	    	
	    	int readCount = client.readInto(buffers[0].array(), 
	    			buffers[0].position(), 
	    			buffers[0].remaining(), 
	    			
	    			buffers[1].array(), 
	    			buffers[1].position(),
	    			buffers[1].remaining());			
	    	
	    	if (readCount<=0) {
	    		//cancel this write
	    		Pipe.unstoreBlobWorkingHeadPosition(output);
	    		return;//come back later when we have some data to read.
	    	} else {
	    		//publish this write	    	
				final int size = Pipe.addMsgIdx(output, RawDataSchema.MSG_CHUNKEDSTREAM_1);
				Pipe.moveBlobPointerAndRecordPosAndLength((int)readCount, output);
				Pipe.confirmLowLevelWrite(output, size);
				Pipe.publishWrites(output);
	    	}
	    }
		
		
	}


}
