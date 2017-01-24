package com.ociweb.iot.valveManifold;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class UARTDataStage extends PronghornStage{

	private final Pipe<RawDataSchema> output;
	
	
	public static void instance(GraphManager gm, Pipe<RawDataSchema> output) {
		new UARTDataStage(gm, output);
		
	}
	public UARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output) {
		super(graphManager, NONE, output);
		this.output = output;
	}

	@Override 
	public void startup() {
		
		
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
	    if (Pipe.hasRoomForWrite(output)) {
		
			final int size = Pipe.addMsgIdx(output, RawDataSchema.MSG_CHUNKEDSTREAM_1);
			
			DataOutputBlobWriter<RawDataSchema> writer = Pipe.outputStream(output);
			writer.openField();
			writer.append("[st1sn100100pn\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"lr-100cc184587lf0pf\"L\"vf0sp80]");
			writer.closeLowLevelField();
			
			Pipe.confirmLowLevelWrite(output, size);
			Pipe.publishWrites(output);
			
			requestShutdown();
	    }
		
		
	}


}
