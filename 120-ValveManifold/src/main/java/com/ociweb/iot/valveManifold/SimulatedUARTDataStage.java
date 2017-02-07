package com.ociweb.iot.valveManifold;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class SimulatedUARTDataStage extends PronghornStage{

	private final Pipe<RawDataSchema> output;
	private int x = 0;
	
	public static void newInstance(GraphManager gm, Pipe<RawDataSchema> output) {
		new SimulatedUARTDataStage(gm, output);
		
	}
	public SimulatedUARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output) {
		super(graphManager, NONE, output);
		this.output = output;
		
		GraphManager.addNota(graphManager, GraphManager.SCHEDULE_RATE, 1_000_000_000, this); //testing once per second.
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
			writer.append("[st1");
			
			writer.append("sn100100");
			writer.append("pn\"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"");			
			writer.append("lr-100");			
			writer.append("cc184587");			
			writer.append("lf0");			
			writer.append("pf\"L\"");			
			writer.append("vf1");
			
			if ((++x&2)==0) {
				writer.append("sp82");
			} else {
				writer.append("sp80");
			}
			
			writer.append("]\n\r\n\r");
			
			// [st0sn101539pn"NX-DCV-SM-BLU-2-I-V0-L0-S0-00"cc31677vf0pf"L"lf0pp1]
			
			writer.closeLowLevelField();
			
			Pipe.confirmLowLevelWrite(output, size);
			Pipe.publishWrites(output);
			
			//requestShutdown();
	    }
		
		
	}


}
