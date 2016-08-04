package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiCommandChannel extends CommandChannel{

	private byte i2cPipeIdx;
	private final byte groveAddr = 0x04;
	
	private Logger logger = LoggerFactory.getLogger(PiCommandChannel.class);

	

	public PiCommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput,  Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> goPipe, byte commandIndex) { 
		super(gm, output, i2cOutput, messagePubSub, goPipe); 
		this.i2cPipeIdx = 1;//TODO: should be different for i2c vs adout. 1 is i2c, 0 is digital

	}

	@Override
	public boolean digitalBlock(int connector, long duration) { 
	    return block(connector,duration); 
	}
	
	@Override
    public boolean analogBlock(int connector, long duration) { 
	    return block(ANALOG_BIT|connector,duration); 
    }
    
	private boolean block(int connector, long duration) { 

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {            

			if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {

			    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, connector);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
				PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, duration*MS_TO_NS);

				PipeWriter.publishWrites(i2cOutput);

                publishGo(1,i2cPipeIdx);
                return true;
			} else {			  
                return false; 
            }
        
		} finally {
		    assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
        
	}
	
    @Override
    public boolean blockUntil(int connector, long time) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            

            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21)) {

                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11, connector);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12, groveAddr);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14, time);

                PipeWriter.publishWrites(i2cOutput);

                publishGo(1,i2cPipeIdx);
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
				
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, connector);        
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);								
				PipeWriter.publishWrites(i2cOutput);
				
                publishGo(1,i2cPipeIdx);
                
				return true;
			}else{
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}
	
	public boolean digitalPulse(int connector) {
	       return digitalPulse(connector, 0);
	}
	


    @Override
    public boolean digitalPulse(int connector, long durationMillis) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            int msgCount = durationMillis > 0 ? 3 : 2;
            assert( Pipe.getPublishBatchSize(i2cOutput)==0);
    
            if (PipeWriter.hasRoomForFragmentOfSize(i2cOutput, msgCount * Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) &&
                PipeWriter.hasRoomForWrite(goPipe) ) {
            
                digitalMessageTemplate[2] = (byte)connector;
                
                //pulse on
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException("Should not have happend since the pipe was already checked.");
                }

                digitalMessageTemplate[3] = (byte)1;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, connector);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                 
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);
                                
                PipeWriter.publishWrites(i2cOutput);
                
                //delay
                if (durationMillis>0) {
                    if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
                        throw new RuntimeException("Should not have happend since the pipe was already checked.");
                    }
                    
                    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, connector);
                    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
                    PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, durationMillis);
                    PipeWriter.publishWrites(i2cOutput);
                }
                
                //pulse off
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException("Should not have happend since the pipe was already checked.");
                }

                digitalMessageTemplate[3] = (byte)0;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, connector);    
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                  
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);
                                
                PipeWriter.publishWrites(i2cOutput);                    
                
                publishGo(msgCount,i2cPipeIdx);
                                    
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
				
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, ANALOG_BIT|connector);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, analogMessageTemplate);
								
				
				logger.debug("CommandChannel sends analogWrite i2c message");
				PipeWriter.publishWrites(i2cOutput);
				
                publishGo(1,i2cPipeIdx);				
				return true;
			}else{
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}





    @Override
    public boolean digitalSetValueAndBlock(int connector, int value, long msDuration) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
    
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, 
                                        Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7) + Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)
                                        )) { 

                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException();
                }
                
                digitalMessageTemplate[2] = (byte)connector;
                digitalMessageTemplate[3] = (byte)value;
             
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, connector);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);
                PipeWriter.publishWrites(i2cOutput);
                
                
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
                    throw new RuntimeException();
                }
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, connector);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, msDuration);
                PipeWriter.publishWrites(i2cOutput);

                publishGo(2,i2cPipeIdx);
                
                return true;
            }else{
                return false;
            }

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }


    @Override
    public boolean analogSetValueAndBlock(int connector, int value, long msDuration) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
    
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, 
                                        Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7) + Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)
                                        )) { 

                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException();
                }
                
                analogMessageTemplate[2] = (byte)connector;
                analogMessageTemplate[3] = (byte)value;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, ANALOG_BIT|connector);      
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);      
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, analogMessageTemplate);                
                PipeWriter.publishWrites(i2cOutput);
                
                
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
                    throw new RuntimeException();
                }
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, ANALOG_BIT|connector);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, msDuration);
                PipeWriter.publishWrites(i2cOutput);

                publishGo(2,i2cPipeIdx);
                
                return true;
            }else{
                return false;
            }

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }

    
    @Override
    public boolean block(long msDuration) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            

            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCHANNEL_22)) {

                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
                PipeWriter.publishWrites(i2cOutput);

                publishGo(1,i2cPipeIdx);
                return true;
            } else {              
                return false; 
            }
        
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }


    

}
