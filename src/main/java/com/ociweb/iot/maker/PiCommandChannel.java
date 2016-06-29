package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ociweb.pronghorn.iot.schema.GoSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;

public class PiCommandChannel extends CommandChannel{

	private Pipe<GroveRequestSchema> output;
	private Pipe<I2CCommandSchema> i2cOutput;
	private Pipe<GoSchema> goPipe;
	private AtomicBoolean aBool = new AtomicBoolean(false);    
	private DataOutputBlobWriter<RawDataSchema> i2cWriter;  
	private int runningI2CCommandCount;
	private final byte i2cIndex = 1;
	private final byte adIndex = 0;

	protected PiCommandChannel(Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<GoSchema> goPipe) {
		super(output, i2cOutput, goPipe);
		this.output = output;
		this.i2cOutput = i2cOutput;  
		this.goPipe = goPipe;

	}


	public boolean digitalBlock(int connector, int duration) { //TODO: Make this work for Pi I2C

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";

		try {            
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCK_220)) {

				System.out.println("write duration of "+duration);
				//TODO: how to detect the wrong ones??

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCK_220_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCK_220_FIELD_DURATION_113, duration);

				PipeWriter.publishWrites(output);

				return true;
			} else {
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public boolean digitalSetValue(int connector, int value) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			boolean msg;
			if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_1)) { //TODO: this needs to be generic 

				byte[] message = {0x04, 0x05, 0x01, 0x02, (byte) connector, (byte) value, 0x00};
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_1_FIELD_BYTEARRAY_2, message);
				System.out.println("CommandChannel sends digitalWrite i2c message");
				PipeWriter.publishWrites(i2cOutput);
				msg = true;
			}else{
				msg = false;
			}
				
			if(PipeWriter.tryWriteFragment(goPipe, GoSchema.MSG_GO_10)) { //TODO: this needs to be generic 

					PipeWriter.writeByte(goPipe, GoSchema.MSG_GO_10_FIELD_PIPEIDX_11, i2cIndex);
					System.out.println("CommandChannel sends digitalWrite i2c go");

					PipeWriter.publishWrites(goPipe);
				return msg;
			} else {
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}


	public boolean analogSetValue(int connector, int value) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {            
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_ANALOGSET_140)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142, value);

				PipeWriter.publishWrites(output);

				return true;
			} else {
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public boolean i2cIsReady() {
		if (null==i2cWriter) {
			i2cWriter = new DataOutputBlobWriter(i2cOutput);//hack for now until we can get this into the scheduler TODO: nathan follow up.
		}


		//TODO: need to set this as a constant driven from the known i2c devices and the final methods
		int maxCommands = 16;

		return PipeWriter.hasRoomForWrite(output) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_1)*maxCommands);

	}

	public DataOutputBlobWriter<RawDataSchema> i2cCommandOpen() {       
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_1)) {
				DataOutputBlobWriter.openField(i2cWriter);
				return i2cWriter;
			} else {
				return null;//can not write try again later.
			}
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public void i2cCommandClose() {  
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			runningI2CCommandCount++;
			DataOutputBlobWriter.closeHighLevelField(i2cWriter, I2CCommandSchema.MSG_COMMAND_1_FIELD_BYTEARRAY_2);
			PipeWriter.publishWrites(i2cOutput);
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public boolean i2cFlushBatch() {        
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_I2CWRITE_400) ) { 
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_I2CWRITE_400_FIELD_MESSAGECOUNT_410, runningI2CCommandCount);
				PipeWriter.publishWrites(output);
				runningI2CCommandCount = 0;                
				return true;
			}
			return false;            
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}


	private boolean enterBlockOk() {
		return aBool.compareAndSet(false, true);
	}

	private boolean exitBlockOk() {
		return aBool.compareAndSet(true, false);
	}


}
