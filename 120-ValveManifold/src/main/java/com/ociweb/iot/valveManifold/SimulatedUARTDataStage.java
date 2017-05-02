package com.ociweb.iot.valveManifold;

import java.util.Random;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
import com.ociweb.pronghorn.util.Appendables;

public class SimulatedUARTDataStage extends PronghornStage{

	private final Pipe<RawDataSchema> output;

	private final int valveCount;
	private int valve = 0;
	private long nextRun;
	private final int countBase;
	private final boolean canFail;
	private String manifold;
	
	String[] snArray;
	String[] pnArray;
	int[]    ccArray;
	
	String[] pfArray = new String[] {"N","L","L","L" ,"L","L","L","N" ,"N","N","H","H" ,"H","H","H","N"};
	
	String[] spArray = new String[] {"100","90","80","70","60","65","70","75" ,"80","80","80","80" ,"80","80","85","95"};
	String[] ppArray = new String[] {"90","80","70","60" ,"50","30","25","25" ,"35","40","50","70" ,"70","70","70","85"};
	
	
	String[] lfArray = new String[] {"0", "0", "0", "1" , "1", "1", "1", "1" , "1", "1", "1", "0" , "0", "0", "0", "0"};
	String[] vfArray = new String[] {"0", "1", "1", "1" , "1", "1", "1", "1" , "1", "1", "0", "0" , "0", "0", "0", "0"};
		
	// mosquitto_sub -v -t '#' -h apple.local
	
	public static void newInstance(GraphManager gm, Pipe<RawDataSchema> output, String client, int valveCount, int countBase, boolean canFail) {
		new SimulatedUARTDataStage(gm, output, client, valveCount, countBase, canFail);
		
	}
	public SimulatedUARTDataStage(GraphManager graphManager, Pipe<RawDataSchema> output, String manifold, int valveCount, int countBase, boolean canFail) {
		super(graphManager, NONE, output);
		this.output = output;
		this.valveCount = valveCount;
		this.countBase = countBase;
		this.canFail = canFail;
		this.manifold = manifold;
		
		Integer.parseInt(manifold);
		
	}

	@Override 
	public void startup() {
		int i = valveCount;
		snArray = new String[i];
		pnArray = new String[i];
		ccArray = new int[i];

		while (--i>=0) {
			snArray[i] = (i+"010010"+manifold);
			pnArray[i] = "\""+ i + manifold + "NX-DCV-SM-BLU-1-1-VO-L1-SO-OO\"";
			ccArray[i] = countBase+i;
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
					
			writer.append("mn");
			writer.append(manifold);
			
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
			writer.append(((valve == 0 && canFail) ? lfArray[cc&0xF] : lfArray[0]));			
			
			writer.append("pf");
			writer.append("\""+ ((valve == 1 && canFail) ? pfArray[cc&0xF] :pfArray[0]) +"\"");		//L H N 	
			
			writer.append("vf");
			writer.append((valve == 4 && canFail) ? vfArray[cc&0xF] : vfArray[0] );
			
			writer.append("sp");
			writer.append(spArray[cc&0xF]);
			
			writer.append("pp");
			writer.append(ppArray[cc&0xF]);
			
			writer.append("]\n\r\n\r");
			
			// [st0sn101539pn"NX-DCV-SM-BLU-2-I-V0-L0-S0-00"cc31677vf0pf"L"lf0pp1]
			
			writer.closeLowLevelField();
			
			Pipe.confirmLowLevelWrite(output, size);
			Pipe.publishWrites(output);
			
			//requestShutdown();
			
			if (++valve>=valveCount) {
				nextRun = System.currentTimeMillis()+1_000;//1 seconds from now.
				valve = 0;
			}
			
	  }
		
	    
	    
		
	}


}
