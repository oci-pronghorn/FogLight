package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.gl.api.Writable;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.SerialDataSchema;
import com.ociweb.iot.hardware.impl.SerialOutputSchema;
import com.ociweb.pronghorn.pipe.FieldReferenceOffsetManager;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class SerialService {

	private final FogCommandChannel cmd;
	
	public SerialService(FogCommandChannel fogCommandChannel) {
		this.cmd = fogCommandChannel;
		cmd.initFeatures |= FogCommandChannel.SERIAL_WRITER;
	}

	public SerialService(FogCommandChannel fogCommandChannel, int commandCountCapacity, int maxMessageSize) {
		this.cmd = fogCommandChannel;
		FogCommandChannel.growCommandCountRoom(cmd, commandCountCapacity);
		cmd.initFeatures |= FogCommandChannel.SERIAL_WRITER;    
		PipeConfig<SerialOutputSchema> config = MsgCommandChannel.PCM(cmd).getConfig(SerialOutputSchema.class);
		if (FogCommandChannel.isTooSmall(commandCountCapacity, maxMessageSize, config)) {		
			MsgCommandChannel.PCM(cmd).addConfig(Math.max(config.minimumFragmentsOnPipe(), commandCountCapacity),
					           Math.max(config.maxVarLenSize(), maxMessageSize), SerialOutputSchema.class);   
		}
	}

    public boolean hasRoomFor(int messageCount) {
		
		return cmd.goHasRoomFor(messageCount) 
		       && (null==cmd.serialOutput || Pipe.hasRoomForWrite(cmd.serialOutput, FieldReferenceOffsetManager.maxFragmentSize(Pipe.from(cmd.serialOutput))*messageCount));
		
    }
    
    public boolean publishSerial(Writable writable) {
    	assert(writable != null);
		assert((0 != (cmd.initFeatures & FogCommandChannel.SERIAL_WRITER))) : "CommandChannel must be created with SERIAL_WRITER flag";
		        
		if (cmd.goHasRoom() && 
			PipeWriter.tryWriteFragment(cmd.serialOutput, SerialDataSchema.MSG_CHUNKEDSTREAM_1)) {
		
			SerialWriter pw = (SerialWriter) Pipe.outputStream(cmd.serialOutput);
			//logger.warn("pw is {}", pw);
			pw.openField(SerialDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2, cmd);            
		    writable.write(pw);//TODO: cool feature, writable to return false to abandon write.. 
		    
		    pw.closeHighLevelField(SerialDataSchema.MSG_CHUNKEDSTREAM_1_FIELD_BYTEARRAY_2);
		    
		    PipeWriter.publishWrites(cmd.serialOutput);     
		
		    MsgCommandChannel.publishGo(1, HardwareImpl.serialIndex(cmd.builder), cmd);
		    
		    return true;
		    
		} else {
		    return false;
		}    	
    }
	
	/**
	 * start shutdown of the runtime, this can be vetoed or postponed by any shutdown listeners
	 */
	public void requestShutdown() {
		
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			cmd.builder.requestShutdown();
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}
	
}
