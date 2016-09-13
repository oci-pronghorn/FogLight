package com.ociweb.iot.maker;

import java.io.IOException;

import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class PayloadWriter extends DataOutputBlobWriter {

    private final Pipe p;
    private CommandChannel commandChannel;
    private long key;
    private int loc=-1;
    
    public PayloadWriter(Pipe p) {
    	super(p);
    	this.p = p;             
        
    }
        
    public void writeString(CharSequence value) {
        writeUTF(value);
    }
    
    public void writeObject(Object object) {
    	
    	try {
			super.writeObject(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void close() {
    	if (loc!=-1) {
    		publish();
    	}
    	
    	try {
			super.close();
		} catch (IOException e) {
			throw new RuntimeException();
		}
    	
    }
    
    public void publish() {
        if (loc!=-1) {
	        closeHighLevelField(loc);
	        loc = -1;//clear field
	        PipeWriter.publishWrites(p);        
	        commandChannel.publishGo(1,commandChannel.subPipeIdx);
        }
    }

    void openField(int loc, CommandChannel commandChannel) {
    	assert(this.loc == -1) : "Already open for writing, can not open again.";
    	this.commandChannel = commandChannel;
        this.loc = loc;
        DataOutputBlobWriter.openField(this);
    }


}
