package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.file.schema.BlockStorageReceiveSchema;
import com.ociweb.pronghorn.stage.file.schema.BlockStorageXmitSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class RawDataJoiner extends PronghornStage {

	private final Pipe<BlockStorageXmitSchema> saveWrite;
	private final Pipe<BlockStorageReceiveSchema> saveAck;
	private final Pipe<RawDataSchema>[] inputs;
    	
	public RawDataJoiner(GraphManager gm, 
						 Pipe<BlockStorageXmitSchema> saveWrite,
						 Pipe<BlockStorageReceiveSchema> saveAck,
			             Pipe<RawDataSchema> ... inputs) {
		super(gm,join(saveWrite, saveAck),inputs);
		
		this.saveWrite = saveWrite;
		this.saveAck = saveAck;		
		this.inputs = inputs;
	}

	@Override
	public void run() {
		
		//read the inputs, until -1 is found
		//record the data...
		//input size is unknown...
		//chunks? or write all to disk and go back and write lengths? block writer?
		
		
		// TODO Auto-generated method stub
		
	}

}
