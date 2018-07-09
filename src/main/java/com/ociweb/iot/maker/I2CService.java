package com.ociweb.iot.maker;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;

public class I2CService {

	private final FogCommandChannel cmd;
	
	public I2CService(FogCommandChannel fogCommandChannel) {
		this.cmd = fogCommandChannel;
		cmd.initFeatures |= FogCommandChannel.I2C_WRITER;
	}

	public I2CService(FogCommandChannel fogCommandChannel, int commandCountCapacity, int maxMessageSize) {
		this.cmd = fogCommandChannel;
		if (cmd.maxCommands>=0) {
			throw new UnsupportedOperationException("Too late, this method must be called in define behavior.");
		}
		MsgCommandChannel.growCommandCountRoom(cmd, commandCountCapacity);
		cmd.initFeatures |= FogCommandChannel.I2C_WRITER; 
		
		
		PipeConfig<I2CCommandSchema> config = MsgCommandChannel.PCM(cmd).getConfig(I2CCommandSchema.class);
		if (FogCommandChannel.isTooSmall(commandCountCapacity, maxMessageSize, config)) {
			MsgCommandChannel.PCM(cmd).addConfig(Math.max(config.minimumFragmentsOnPipe(), commandCountCapacity),
			           Math.max(config.maxVarLenSize(), maxMessageSize), I2CCommandSchema.class);   
		}
	}
	
	public boolean block(Port port, long durationMilli) {
		return cmd.block(port,durationMilli);
	}
	
	public boolean blockUntil(Port port, long time) {
		 return cmd.blockUntil(port,time);
	}
	
    /**
     * Opens an I2C connection.
     *
     * @param targetAddress I2C address to open a connection to.
     *
     * @return An {@link DataOutputBlobWriter} with an {@link I2CCommandSchema} that's
     *         connected to the specified target address.
     *
     */
    public DataOutputBlobWriter<I2CCommandSchema> i2cCommandOpen(int targetAddress) {       
    	assert((0 != (cmd.initFeatures & FogCommandChannel.I2C_WRITER))) : "CommandChannel must be created with I2C_WRITER flag";
		
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
		
		    if (PipeWriter.tryWriteFragment(cmd.i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
		        PipeWriter.writeInt(cmd.i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);
		        DataOutputBlobWriter<I2CCommandSchema> writer = PipeWriter.outputStream(cmd.i2cOutput);
		        DataOutputBlobWriter.openField(writer);
		        return writer;
		    } else {
		        throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
		    }
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
    }

    /**
     * Triggers a delay for a given I2C address.
     *
     * @param targetAddress I2C address to trigger a delay on.
     * @param durationNanos Time in nanoseconds to delay.
     */
    public void i2cDelay(int targetAddress, long durationNanos) {
    	assert((0 != (cmd.initFeatures & FogCommandChannel.I2C_WRITER))) : "CommandChannel must be created with I2C_WRITER flag";
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
		    if (++cmd.runningI2CCommandCount > cmd.maxCommands) {
		        throw new UnsupportedOperationException("too many commands, found "+cmd.runningI2CCommandCount+" but only left room for "+cmd.maxCommands);
		    }
		
		    if (cmd.goHasRoom() && PipeWriter.tryWriteFragment(cmd.i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
		
		        PipeWriter.writeInt(cmd.i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, targetAddress);
		        PipeWriter.writeInt(cmd.i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, targetAddress);
		        PipeWriter.writeLong(cmd.i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, durationNanos);
		
		        PipeWriter.publishWrites(cmd.i2cOutput);
		
		    }else {
		        throw new UnsupportedOperationException("Pipe is too small for large volume of i2c data");
		    }    
		    
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}        
    }

    public boolean i2cIsReady() {
    	return i2cIsReady(1);
    }
    /**
     * @return True if the I2C bus is ready for communication, and false otherwise.
     */
    public boolean i2cIsReady(int requestedCommandCount) {
    	assert((0 != (cmd.initFeatures & FogCommandChannel.I2C_WRITER))) : "CommandChannel must be created with I2C_WRITER flag";
		assert(null!=cmd.i2cOutput) : "pipe must not be null";
		assert(Pipe.isInit(cmd.i2cOutput)) : "pipe must be initialized";    	
		return cmd.goHasRoom() && PipeWriter.hasRoomForFragmentOfSize(cmd.i2cOutput, FogCommandChannel.SIZE_OF_I2C_COMMAND*requestedCommandCount);
    }

    /**
     * Flushes all awaiting I2C data to the I2C bus for consumption.
     */
    public void i2cFlushBatch() {
        assert ((0 != (cmd.initFeatures & FogCommandChannel.I2C_WRITER))) : "CommandChannel must be created with I2C_WRITER flag";
		if (cmd.runningI2CCommandCount > 0) {
		    assert (cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		    try {
		    	FogCommandChannel.releaseI2CTraffic(cmd);
		        cmd.runningI2CCommandCount = 0;
		    } finally {
		        assert (cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		    }
		}
    }

    public int i2cCommandClose(DataOutputBlobWriter<I2CCommandSchema> writer) {
    	assert((0 != (cmd.initFeatures & FogCommandChannel.I2C_WRITER))) : "CommandChannel must be created with I2C_WRITER flag";
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
		    if (++cmd.runningI2CCommandCount > cmd.maxCommands) {
		        throw new UnsupportedOperationException("too many commands, found "+cmd.runningI2CCommandCount+" but only left room for "+cmd.maxCommands);
		    }
		
		    int bytesWritten = DataOutputBlobWriter.closeHighLevelField(writer, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
		    PipeWriter.publishWrites(cmd.i2cOutput);
		    return bytesWritten;
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}  
    }
	
	/**
	 * start shutdown of the runtime, this can be vetoed or postponed by any shutdown listeners
	 */
	public void triggerShutdownRuntime() {
		
		assert(cmd.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			cmd.builder.triggerShutdownProcess();
		} finally {
		    assert(cmd.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}
}
