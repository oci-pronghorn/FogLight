package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class RawDataSplitter extends PronghornStage {

	private final Pipe<RawDataSchema> source; 
    private final Pipe<RawDataSchema>[] targets;
    
    
    /**
     * Read raw data and route blocks to the right target based on data found.
     * On the stream a Short indicates destination target
     * The Short is followed by a Long which is the number of bytes to send.
     * The Short and Long are both big end in.
     * @param gm GraphManager
     * @param source RawDataSchema Pipe to read from
     * @param targets an array of outgoing target pipes
     */
	public RawDataSplitter(GraphManager gm, 
			               Pipe<RawDataSchema> source, 
			               Pipe<RawDataSchema> ... targets) {
		super(gm, source,  targets);
		this.source = source;
		this.targets = targets;
	}

	@Override
	public void run() {
		
		while (Pipe.hasContentToRead(source)) {
			
		}
		
		// TODO Auto-generated method stub
		
	}
	

}
