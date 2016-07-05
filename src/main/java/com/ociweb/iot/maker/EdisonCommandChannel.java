package com.ociweb.iot.maker;

import java.util.concurrent.atomic.AtomicBoolean;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class EdisonCommandChannel extends CommandChannel{

	private Pipe<GroveRequestSchema> output;
	private Pipe<I2CCommandSchema> i2cOutput;
	private Pipe<TrafficOrderSchema> goPipe;
	private AtomicBoolean aBool = new AtomicBoolean(false);    
	private DataOutputBlobWriter<RawDataSchema> i2cWriter;  
	private int runningI2CCommandCount;
	private final byte adIndex = (byte)0;

	public EdisonCommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,Pipe<TrafficOrderSchema> goPipe) {
			super(gm, output, i2cOutput, goPipe);
	 		this.output = output;
			this.i2cOutput = i2cOutput;       
			this.goPipe = goPipe;

	}


	public boolean digitalBlock(int connector, int duration) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			boolean msg;
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCK_220)) {

				System.out.println("write duration of "+duration);
				//TODO: how to detect the wrong ones??

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCK_220_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCK_220_FIELD_DURATION_113, duration);

				PipeWriter.publishWrites(output);
				
				msg=true;
			}else{
				msg=false;
			}
			if(msg&&PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) { //TODO: this needs to be generic 

				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, (byte)0);
				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, (byte) 1);
				System.out.println("The Edison CommandChannel sends the writeBlock to Go");					

				PipeWriter.publishWrites(goPipe);
			return true;
			
			}else {
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
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) { //TODO: this needs to be generic 

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, value);
				System.out.println("Edison CommandChannel sends digitalWrite message ");
				PipeWriter.publishWrites(output);
				msg = true;
			}else{
				msg = false;
			}
				
			if(msg&&PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) { //TODO: this needs to be generic 

					PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, (byte)0);
					PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, (byte) 1);
					System.out.println("The Edison CommandChannel sends the WriteByte to Go");					

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
			boolean msg;
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_ANALOGSET_140)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142, value);
				System.out.println("Edison CommandChannel sends analogWrite message ");
				PipeWriter.publishWrites(output);
				msg = true;
			} else {
				msg = false;
			}
			if(msg&&PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) { //TODO: this needs to be generic 

				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, (byte)0);
				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, (byte) 1);
				System.out.println("The Edison CommandChannel sends the AnalogWrite to Go");					

				PipeWriter.publishWrites(goPipe);
			return msg;
		} else {
			return false;
		}
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public boolean analogBlock(int connector, int value, int duration) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {        
			boolean msg;
			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_CONNECTOR_141,connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_VALUE_142,    value);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSETANDBLOCK_240_FIELD_DURATION_113, duration);
				System.out.println("Edison CommandChannel sends analogBlock message ");
				PipeWriter.publishWrites(output);
				msg = true;
			} else {
				msg = false;
			}
			if(msg&&PipeWriter.tryWriteFragment(goPipe, TrafficOrderSchema.MSG_GO_10)) { //TODO: this needs to be generic 

				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_PIPEIDX_11, (byte)0);
				PipeWriter.writeByte(goPipe, TrafficOrderSchema.MSG_GO_10_FIELD_COUNT_12, (byte) 1);
				System.out.println("The Edison CommandChannel sends the AnalogBlock to Go");					

				PipeWriter.publishWrites(goPipe);
			return msg;
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

		return PipeWriter.hasRoomForWrite(output) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands);

	}

	public DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress) {       
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
			    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);
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
			DataOutputBlobWriter.closeHighLevelField(i2cWriter, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2);
			PipeWriter.publishWrites(i2cOutput);
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	public boolean i2cFlushBatch() {        
//		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
//		try {
//			if (PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_I2CWRITE_400) ) { 
//				PipeWriter.writeInt(output, GroveRequestSchema.MSG_I2CWRITE_400_FIELD_MESSAGECOUNT_410, runningI2CCommandCount);
//				PipeWriter.publishWrites(output);
//				runningI2CCommandCount = 0;                
//				return true;
//			}
//			return false;            
//		} finally {
//			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
//		}
	    return false;
	}


	private boolean enterBlockOk() {
		return aBool.compareAndSet(false, true);
	}

	private boolean exitBlockOk() {
		return aBool.compareAndSet(true, false);
	}


}
