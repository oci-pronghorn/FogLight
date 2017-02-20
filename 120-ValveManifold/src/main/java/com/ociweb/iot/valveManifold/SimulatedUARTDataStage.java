package com.ociweb.iot.valveManifold;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

public class SimulatedUARTDataStage extends PronghornStage{

	private final Pipe<RawDataSchema> output;
	private int x = 0;
	private final int VALVECOUNT = 6;
	private int valve = 0;
	private long nextRun;
	
	String[] snArray;
	String[] pnArray;
	int[]    ccArray;
	
	String[] pfArray = new String[] {"H","L","N","N" ,"N","N","N","N" ,"N","N","N","N" ,"N","N","N","N"};
	String[] spArray = new String[] {"100","10","80","80" ,"80","80","80","80" ,"80","80","80","80" ,"80","80","80","80"};
	
	String[] lfArray = new String[] {"1", "0", "0", "0" , "0", "0", "0", "0" , "0", "0", "0", "0" , "0", "0", "0", "0"};
	String[] vfArray = new String[] {"0", "1", "0", "0" , "0", "0", "0", "0" , "0", "0", "0", "0" , "0", "0", "0", "0"};
		
	
	public static void newInstance(GraphManager gm, Pipe<RawDataSchema> output) {
		new SimulatedUARTDataStage(gm, output);
		
	}
	public SimulatedUARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output) {
		super(graphManager, NONE, output);
		this.output = output;
		
	}

	@Override 
	public void startup() {
		int i = VALVECOUNT;
		snArray = new String[i];
		pnArray = new String[i];
		ccArray = new int[i];
		while (--i>=0) {
			snArray[i] = (i+"010010");
			pnArray[i] = "\""+ i +"NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"";
			ccArray[i] = 100+i;
		}
		
		
	}
	
	@Override
	public void shutdown() {
		Pipe.spinBlockForRoom(output, Pipe.EOF_SIZE);
		Pipe.publishEOF(output);
	}
	
	@Override
	public void run() {
		
		long now = System.currentTimeMillis();
		if (now<nextRun) {
			return;
		}		
		
	    if (Pipe.hasRoomForWrite(output)) {
		
			final int size = Pipe.addMsgIdx(output, RawDataSchema.MSG_CHUNKEDSTREAM_1);
			
			DataOutputBlobWriter<RawDataSchema> writer = Pipe.outputStream(output);
			writer.openField();
			writer.append("[");
			
			writer.append("st");
			Appendables.appendValue(writer, valve);
					
			writer.append("sn");
			writer.append(snArray[valve]);
			
			writer.append("pn");
			writer.append(pnArray[valve]);			
			
			writer.append("lr");
			writer.append("-100");			
			
			writer.append("cc");
			int cc = ++ccArray[valve];
			Appendables.appendValue(writer, cc);			
			
			writer.append("lf");
			writer.append(lfArray[cc&0xF]);			
			
			writer.append("pf");
			writer.append("\""+ pfArray[cc&0xF]+"\"");		//L H N 	
			
			writer.append("vf");
			writer.append(vfArray[cc&0xF]);
			
			writer.append("sp");
			writer.append(spArray[cc&0xF]);
			
			writer.append("pp");
			writer.append("1");
			
			writer.append("]\n\r\n\r");
			
			// [st0sn101539pn"NX-DCV-SM-BLU-2-I-V0-L0-S0-00"cc31677vf0pf"L"lf0pp1]
			
			writer.closeLowLevelField();
			
			Pipe.confirmLowLevelWrite(output, size);
			Pipe.publishWrites(output);
			
			//requestShutdown();
			
			if (++valve>=VALVECOUNT) {
				nextRun = System.currentTimeMillis()+1000;//1 second from now.
				valve = 0;
			}
			
	  }
		
	    
	    
		
	}


}
