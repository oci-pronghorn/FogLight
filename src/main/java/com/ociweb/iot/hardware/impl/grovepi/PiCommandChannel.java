package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.schema.MessagePubSub;
import com.ociweb.pronghorn.schema.NetRequestSchema;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiCommandChannel extends CommandChannel{

	private byte i2cPipeIdx;
	private final byte groveAddr = 0x04;
	
	private Logger logger = LoggerFactory.getLogger(PiCommandChannel.class);

	

	public PiCommandChannel(GraphManager gm, HardwareImpl hardware, PipeConfig<GroveRequestSchema> output, PipeConfig<I2CCommandSchema> i2cOutput,  
			 PipeConfig<MessagePubSub> pubSubConfig,
             PipeConfig<NetRequestSchema> netRequestConfig,
             PipeConfig<TrafficOrderSchema> goPipe, byte commandIndex) { 
		super(gm, hardware, output, i2cOutput, pubSubConfig, netRequestConfig, goPipe); 
		this.i2cPipeIdx = 1;//TODO: should be different for i2c vs adout. 1 is i2c, 0 is digital

	}

	@Override
    public boolean block(Port port, long duration) { 
	    return block((port.isAnalog()?ANALOG_BIT:0)|port.port,duration); 
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
    public boolean blockUntil(Port port, long time) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {            

            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21)) {

                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11, port.port);
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
    
    

	
	public boolean digitalPulse(Port port) {
	       return digitalPulse(port, 0);
	}
	

    @Override
    public boolean digitalPulse(Port port, long durationNanos) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
        	if (port.isAnalog()) {
        		throw new UnsupportedOperationException();
        	}
        		
            int msgCount = durationNanos > 0 ? 3 : 2;
            assert( Pipe.getPublishBatchSize(i2cOutput)==0);
    
            if (PipeWriter.hasRoomForFragmentOfSize(i2cOutput, msgCount * Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) &&
                PipeWriter.hasRoomForWrite(goPipe) ) {
            
                digitalMessageTemplate[2] = (byte)port.port;
                
                //pulse on
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException("Should not have happend since the pipe was already checked.");
                }

                digitalMessageTemplate[3] = (byte)1;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, port.port);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                 
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, digitalMessageTemplate);
                                
                PipeWriter.publishWrites(i2cOutput);
                
                //delay
                if (durationNanos>0) {
                    if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
                        throw new RuntimeException("Should not have happend since the pipe was already checked.");
                    }
                    
                    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, port.port);
                    PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
                    PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, durationNanos);
                    PipeWriter.publishWrites(i2cOutput);
                }
                
                //pulse off
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException("Should not have happend since the pipe was already checked.");
                }

                digitalMessageTemplate[3] = (byte)0;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, port.port);    
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


	
	//Build templates like this once that can be populated and sent without redefining the part that is always the same.
	private final byte[] digitalMessageTemplate = {0x01, 0x02, -1, -1, 0x00};
	private final byte[] analogMessageTemplate = {0x01, 0x04, -1, -1, 0x00};
	
	public boolean setValue(Port port, int value) {

		int mask = port.isAnalog()? ANALOG_BIT:0;
		byte[] template = port.isAnalog()? analogMessageTemplate : digitalMessageTemplate;
		
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) { 

				template[2] = (byte)port.port;
				template[3] = (byte)value;		
				
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, mask|port.port);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, template);
								
				
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
    public boolean setValueAndBlock(Port port, int value, long msDuration) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
        	int mask = port.isAnalog() ? ANALOG_BIT : 0;
        	byte[] template = port.isAnalog()? analogMessageTemplate : digitalMessageTemplate;
        	        	
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, 
                                        Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_COMMAND_7) + Pipe.sizeOf(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)
                                        )) { 

                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) {
                    throw new RuntimeException();
                }
                
                template[2] = (byte)port.port;
                template[3] = (byte)value;
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, mask|port.port);      
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);      
                PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, template);                
                PipeWriter.publishWrites(i2cOutput);
                
                
                if (!PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {
                    throw new RuntimeException();
                }
                
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, mask|port.port);
                PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
                PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
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
