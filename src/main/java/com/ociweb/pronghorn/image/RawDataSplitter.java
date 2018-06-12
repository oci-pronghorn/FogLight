package com.ociweb.pronghorn.image;

import com.ociweb.pronghorn.pipe.DataInputBlobReader;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class RawDataSplitter extends PronghornStage {

	private final Pipe<RawDataSchema> source; 
    private final Pipe<RawDataSchema>[] targets;
    private int targetPipeIdx = -1;
    private long targetRemaining;
	private DataInputBlobReader<RawDataSchema> inputStream;
    private boolean isShuttingDown = false;
	
    public static RawDataSplitter newInstance(GraphManager gm, 
            	Pipe<RawDataSchema> source, 
            	Pipe<RawDataSchema> ... targets) {
    	return new RawDataSplitter(gm, source, targets);
    }
    
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
		this.inputStream = Pipe.inputStream(source);
	}

	@Override
	public void run() {
		
		processData();//even called when shutting down so we can clear the pipes.
		
		if (!isShuttingDown) {
							
			while (Pipe.hasContentToRead(source)) {
				int msgIdx = Pipe.takeMsgIdx(source);
				if (msgIdx>=0) {
					DataInputBlobReader.accumLowLevelAPIField(inputStream);
					Pipe.confirmLowLevelRead(source, Pipe.sizeOf(source, RawDataSchema.MSG_CHUNKEDSTREAM_1));
					Pipe.readNextWithoutReleasingReadLock(source);
									
					processData();
				} else {
					isShuttingDown = true;
				}
			}
			
		} else {
			//shutdown process
			int i = targets.length;
			while (--i>=0) {
				if (!Pipe.hasRoomForWrite(targets[i])) {
					return;//try again later
				}
			}
			Pipe.publishEOF(targets);
			requestShutdown();
		}
	}

	private void processData() {
		DataInputBlobReader<RawDataSchema> inputStream = Pipe.inputStream(source);
		
		if (targetPipeIdx<0) {
			//need to read next block
			if (inputStream.available( )>= 10) { //short plus long is 2 + 8
				targetPipeIdx = inputStream.readShort();
				targetRemaining = inputStream.readLong();
				Pipe.releasePendingAsReadLock(source, 8);
			}
		}
	
		//move the data
		while (targetRemaining>0 && inputStream.available()>0 && Pipe.hasRoomForWrite(targets[targetPipeIdx]) ) {
			
			Pipe<RawDataSchema> t = targets[targetPipeIdx];
			int toCopyLength = (int)Math.min(Math.min(t.maxVarLen, targetRemaining), inputStream.available());
			
			int size = Pipe.addMsgIdx(t, RawDataSchema.MSG_CHUNKEDSTREAM_1);
			DataOutputBlobWriter<RawDataSchema> outputStream = Pipe.openOutputStream(t);
			inputStream.readInto(outputStream, toCopyLength);
			DataOutputBlobWriter.closeLowLevelField(outputStream);
			Pipe.confirmLowLevelWrite(t, size);
			Pipe.publishWrites(t);
			
			Pipe.releasePendingAsReadLock(source, toCopyLength);
							
			targetRemaining -= toCopyLength;
			if (targetRemaining<=0) {
				if (inputStream.available( )>= 10) { //short plus long is 2 + 8
					targetPipeIdx = inputStream.readShort();
					targetRemaining = inputStream.readLong();
					Pipe.releasePendingAsReadLock(source, 8);
				} else {
					targetPipeIdx = -1;
				}
			}
		}
	}

}
