package com.ociweb.iot.hardware.impl;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DefaultCommandChannel extends CommandChannel{


	public DefaultCommandChannel(GraphManager gm, HardwareImpl hardware, Pipe<GroveRequestSchema> output, Pipe<I2CCommandSchema> i2cOutput, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> goPipe) {
			super(gm, hardware, output, i2cOutput, messagePubSub, goPipe);
			assert(Pipe.isForSchema(outputPipes[pinPipeIdx], GroveRequestSchema.instance));
			assert(Pipe.isForSchema(outputPipes[i2cPipeIdx], I2CCommandSchema.instance));
	}
	

	private boolean block(int connector, long duration) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220)) {

				PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, duration*MS_TO_NS);
				
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
	public boolean setValue(Port port, int value) {

		int mask = 0;
		int msgId;
		int msgField1;
		int msgField2;
		if (port.isAnalog()) {
			mask = ANALOG_BIT;
			msgId= GroveRequestSchema.MSG_ANALOGSET_140;
			msgField1 = GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141;
			msgField2 = GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142;
		} else {
			msgId= GroveRequestSchema.MSG_DIGITALSET_110;
			msgField1 = GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111;
			msgField2 = GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112;
		}
		
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {        
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, msgId)) {

				PipeWriter.writeInt(output, msgField1, mask|port.port);
				PipeWriter.writeInt(output, msgField2, value);
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
	public boolean digitalPulse(Port port) {
	    return digitalPulse(port, 0);
	}
	@Override
	public boolean digitalPulse(Port port, long durationNanos) {
			if (port.isAnalog()) {
				throw new UnsupportedOperationException();
			}
					
	        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
	        try {
	            int msgCount = durationNanos > 0 ? 3 : 2;
	            
	            if (PipeWriter.hasRoomForFragmentOfSize(output, 2 * Pipe.sizeOf(i2cOutput, GroveRequestSchema.MSG_DIGITALSET_110)) && 
	                PipeWriter.hasRoomForWrite(goPipe) ) {           
	            
	                //Pulse on
	                if (!PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                   throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                }

	                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, port.port);
	                PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, 1);

	                PipeWriter.publishWrites(output);
	                
	                //duration
	                //delay
	                if (durationNanos>0) {
	                    if (!PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220)) {
	                        throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }
	                    PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, port.port);
	                    PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, durationNanos);
	                    PipeWriter.publishWrites(output);
	                }
	                

	                //Pulse off
	                if (!PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                       throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }

                    PipeWriter.writeInt(output, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, port.port);
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
	




    @Override
    public boolean setValueAndBlock(Port port, int value, long msDuration) {
    	
    	int mask = 0;
		int msgId;
		int msgField1;
		int msgField2;
		if (port.isAnalog()) {
			mask = ANALOG_BIT;
			msgId= GroveRequestSchema.MSG_ANALOGSET_140;
			msgField1 = GroveRequestSchema.MSG_ANALOGSET_140_FIELD_CONNECTOR_141;
			msgField2 = GroveRequestSchema.MSG_ANALOGSET_140_FIELD_VALUE_142;
		} else {
			msgId= GroveRequestSchema.MSG_DIGITALSET_110;
			msgField1 = GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111;
			msgField2 = GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112;
		}
		
		
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {        
            
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(output, Pipe.sizeOf(output, msgId)+
                                                                                                  Pipe.sizeOf(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220)  ) ) {
            
            	
                PipeWriter.tryWriteFragment(output, msgId);
                PipeWriter.writeInt(output, msgField1, mask|port.port);
                PipeWriter.writeInt(output, msgField2, value);
                PipeWriter.publishWrites(output);
                            
                PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220);
                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, mask|port.port);
                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
                
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
    public boolean block(Port port, long duration) { 
        return block((port.isAnalog()?ANALOG_BIT:0) |port.port,duration); 
    }

    @Override
    public boolean block(long msDuration) {

        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCHANNEL_22)) {

                PipeWriter.writeLong(output, GroveRequestSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
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
    public boolean blockUntil(Port port, long time) {
        assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
        try {
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(output, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221)) {

                PipeWriter.writeInt(output, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111, port.port);
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
