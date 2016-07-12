package com.ociweb.iot.maker;

import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiCommandChannel extends CommandChannel{

	private Pipe<GroveRequestSchema> output;
	private Pipe<I2CCommandSchema> i2cOutput;
	
	private DataOutputBlobWriter<RawDataSchema> i2cWriter;  
	private int runningI2CCommandCount;
	private byte i2cPipeIdx;
	private byte groveAddr = 0x04;
	

    //TODO: need to set this as a constant driven from the known i2c devices and the final methods
    private final int maxCommands = 16;
	

	public PiCommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<TrafficOrderSchema> goPipe, byte commandIndex) { 
		super(gm, goPipe, output, i2cOutput, goPipe); //yes this is supposed to pass in goPipe twice. 
		this.output = output;
		this.i2cOutput = i2cOutput;  
		this.i2cPipeIdx = 1;//TODO: should be different for i2c vs adout. 1 is i2c, 0 is digital

	}


	public boolean block(int connector, int duration) { //TODO: Make this work for Pi I2C

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {            

			if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCK_10)) {

				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCK_10_FIELD_ADDRESS_12, connector);
				PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCK_10_FIELD_DURATION_13, duration);

				PipeWriter.publishWrites(i2cOutput);

                int count = 1;
                publishGo(count,i2cPipeIdx);
                return true;
			} else {			  
                return false; 
            }
        
		} finally {
		    assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
        
	}
	
	
	//Build templates like this once that can be populated and sent without redefining the part that is always the same.
	private final byte[] digitalMessageTemplate = {0x01, 0x02, -1, -1, 0x00};

	public boolean digitalSetValue(int connector, int value) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
	
			if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) { 

				digitalMessageTemplate[2] = (byte)connector;
				digitalMessageTemplate[3] = (byte)value;
				
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCK_10_FIELD_ADDRESS_12, groveAddr);
                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);
								
				
				System.out.println("CommandChannel sends digitalWrite i2c message");
				PipeWriter.publishWrites(i2cOutput);
				
                int count = 1;
                publishGo(count,i2cPipeIdx);
                
				return true;
			}else{
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

 
	private final byte[] analogMessageTemplate = {0x01, 0x04, -1, -1, 0x00};
	
	public boolean analogSetValue(int connector, int value) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) { 

				analogMessageTemplate[2] = (byte)connector;
				analogMessageTemplate[3] = (byte)value;
				
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCK_10_FIELD_ADDRESS_12, groveAddr);
                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, analogMessageTemplate);
								
				
				System.out.println("CommandChannel sends analogWrite i2c message");
				PipeWriter.publishWrites(i2cOutput);
				
                int count = 1;
                publishGo(count,i2cPipeIdx);
				
				return true;
			}else{
				
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

		return PipeWriter.hasRoomForWrite(goPipe) &&
		       PipeWriter.hasRoomForWrite(output) && 
		       PipeWriter.hasRoomForFragmentOfSize(i2cOutput, Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)*maxCommands);

	}

//	public Pipe<I2CCommandSchema> i2cCommandOpen(int targetAddress) {       
//		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
//		try {
//			if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
//			    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);
//				//DataOutputBlobWriter.openField(i2cWriter);
//				System.out.println("Got i2cWriter");
//				return i2cOutput;
//			} else {
//				return null;//can not write try again later.
//			}
//		} finally {
//			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
//		}
//	}
//
//	public void i2cCommandClose() {  
//		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
//		try {
//			runningI2CCommandCount++;
//			PipeWriter.publishWrites(i2cOutput);
//			System.out.println("i2c Command Closed "+runningI2CCommandCount);
//		} finally {
//			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
//		}
//	}
	public DataOutputBlobWriter<RawDataSchema> i2cCommandOpen(int targetAddress) {       
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
			    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, targetAddress);
				DataOutputBlobWriter.openField(i2cWriter);
				System.out.println("sending address "+targetAddress);
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
			System.out.println("i2c Command Closed "+runningI2CCommandCount);
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
		
	}

	public boolean i2cFlushBatch() {        
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
            int count = 1;
            publishGo(count,i2cPipeIdx);
            
			runningI2CCommandCount =0;
			return true;
       
		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}



    @Override
    public boolean block(int msDuration) {
        throw new UnsupportedOperationException("TODO: implment this, send.");
        // TODO Auto-generated method stub
        //return false;
    }


    @Override
    public boolean digitalSetValueAndBlock(int connector, int value, int msDuration) {
        throw new UnsupportedOperationException("TODO: implment this, send.");
        // TODO Auto-generated method stub
        //return false;
    }


    @Override
    public boolean analogSetValueAndBlock(int connector, int value, int msDuration) {
        throw new UnsupportedOperationException("TODO: implment this, send.");
        // TODO Auto-generated method stub
        //return false;
    }


}
