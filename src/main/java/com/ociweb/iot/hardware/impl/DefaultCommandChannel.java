package com.ociweb.iot.hardware.impl;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.DataOutputBlobWriter;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DefaultCommandChannel extends CommandChannel{


	public DefaultCommandChannel(GraphManager gm, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> goPipe) {
			super(gm, output, i2cOutput, messagePubSub, goPipe);
			assert(Pipe.isForSchema(outputPipes[pinPipeIdx], GroveRequestSchema.instance));
			assert(Pipe.isForSchema(outputPipes[i2cPipeIdx], I2CCommandSchema.instance));
	}
	

	private boolean block(int connector, long duration) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_DURATION_113, duration);
				
				PipeWriter.publishWrites(output);
				
                publishGo(1,pinPipeIdx);
                
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
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, value);

				PipeWriter.publishWrites(output);
                
                publishGo(1,pinPipeIdx);
				
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
	public boolean digitalPulse(int connector, long durationMillis) {

	        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
	        try {
	            int msgCount = durationMillis > 0 ? 3 : 2;
	            
	            if (PipeWriter.hasRoomForFragmentOfSize(i2cOutput, 2 * Pipe.sizeOf(i2cOutput, GroveRequestSchema.MSG_DIGITALSET_110)) && 
	                PipeWriter.hasRoomForWrite(goPipe) ) {           
	            
	                //Pulse on
	                if (!PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                   throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                }

	                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
	                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, 1);

	                PipeWriter.publishWrites(output);
	                
	                //duration
	                //delay
	                if (durationMillis>0) {
	                    if (!PipeWriter.tryWriteFragment(i2cOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220)) {
	                        throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }
	                    PipeWriter.writeInt(i2cOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_CONNECTOR_111, connector);
	                    PipeWriter.writeLong(i2cOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_DURATION_113, durationMillis);
	                    PipeWriter.publishWrites(i2cOutput);
	                }
	                

	                //Pulse off
	                if (!PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                       throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }

                    PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
                    PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, 0);

                    PipeWriter.publishWrites(output);               
	                
	                publishGo(msgCount,pinPipeIdx);

	                return true;
	            }else{
	                return false;
	            }
	        } finally {
	            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
	        }
	}
	

	public boolean analogSetValue(int connector, int value) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {        
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_ANALOGSET_140)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141, ANALOG_BIT|connector);
				PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142, value);
				PipeWriter.publishWrites(output);
			                
				
                publishGo(1,pinPipeIdx);                
                return true;
			} else {
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
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(output, Pipe.sizeOf(output, GroveRequestSchema.MSG_DIGITALSET_110)+
                                                                                                  Pipe.sizeOf(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220)  ) ) {

                PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, value);

                PipeWriter.publishWrites(output);
                
                PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_DURATION_113, msDuration);
                
                PipeWriter.publishWrites(output);
                
                publishGo(2,pinPipeIdx);
                
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
            
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(output, Pipe.sizeOf(output, GroveRequestSchema.MSG_ANALOGSET_140)+
                                                                                                  Pipe.sizeOf(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220)  ) ) {
            
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141, ANALOG_BIT|connector);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142, value);
                PipeWriter.publishWrites(output);
                            
                PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_CONNECTOR_111, ANALOG_BIT|connector);
                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTIONMS_220_FIELD_DURATION_113, msDuration);
                
                PipeWriter.publishWrites(output);
                
                publishGo(2,pinPipeIdx);
                
                return true;
            } else {
                return false;
            }
        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
        
    }
	


    @Override
    public boolean digitalBlock(int connector, long duration) { 
        return block(connector,duration); 
    }
    
    @Override
    public boolean analogBlock(int connector, long duration) { 
        return block(ANALOG_BIT|connector,duration); 
    }
    
    @Override
    public boolean block(long msDuration) {

        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCHANNELMS_22)) {

                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCHANNELMS_22_FIELD_DURATION_13, msDuration);
                PipeWriter.publishWrites(output);
                
                publishGo(1,pinPipeIdx);
                
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
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221)) {

                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111, connector);
                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114, time);
                PipeWriter.publishWrites(output);
                
                int count = 1;
                publishGo(count,pinPipeIdx);
                
                return true;
            } else {
                return false;
            }

        } finally {
            assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
        }
    }







}
