package com.ociweb.iot.hardware.impl;

import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DefaultCommandChannel extends CommandChannel{


	public DefaultCommandChannel(GraphManager gm, HardwareImpl hardware, PipeConfig<GroveRequestSchema> output, PipeConfig<I2CCommandSchema> i2cOutput,
			 PipeConfig<MessagePubSub> pubSubConfig,
             PipeConfig<NetRequestSchema> netRequestConfig,
             PipeConfig<TrafficOrderSchema> goPipe) {
			super(gm, hardware, output, i2cOutput, pubSubConfig, netRequestConfig, goPipe);

	}
	

	private boolean block(int connector, long duration) {

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220)) {

				PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, connector);
				PipeWriter.writeLong(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, duration*MS_TO_NS);
				
				PipeWriter.publishWrites(pinOutput);
				
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
			if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(pinOutput, msgId)) {

				PipeWriter.writeInt(pinOutput, msgField1, mask|port.port);
				PipeWriter.writeInt(pinOutput, msgField2, value);
				PipeWriter.publishWrites(pinOutput);
			                
				
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
	            
	            if (PipeWriter.hasRoomForFragmentOfSize(pinOutput, 2 * Pipe.sizeOf(i2cOutput, GroveRequestSchema.MSG_DIGITALSET_110)) && 
	                PipeWriter.hasRoomForWrite(goPipe) ) {           
	            
	                //Pulse on
	                if (!PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                   throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                }

	                PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, port.port);
	                PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, 1);

	                PipeWriter.publishWrites(pinOutput);
	                
	                //duration
	                //delay
	                if (durationNanos>0) {
	                    if (!PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220)) {
	                        throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }
	                    PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, port.port);
	                    PipeWriter.writeLong(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, durationNanos);
	                    PipeWriter.publishWrites(pinOutput);
	                }
	                

	                //Pulse off
	                if (!PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110)) {
	                       throw new RuntimeException("Should not have happend since the pipe was already checked.");
	                    }

                    PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_CONNECTOR_111, port.port);
                    PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_DIGITALSET_110_FIELD_VALUE_112, 0);

                    PipeWriter.publishWrites(pinOutput);               
	                
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
            
            if (PipeWriter.hasRoomForWrite(goPipe) && PipeWriter.hasRoomForFragmentOfSize(pinOutput, Pipe.sizeOf(pinOutput, msgId)+
                                                                                                  Pipe.sizeOf(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220)  ) ) {
            
            	
                PipeWriter.tryWriteFragment(pinOutput, msgId);
                PipeWriter.writeInt(pinOutput, msgField1, mask|port.port);
                PipeWriter.writeInt(pinOutput, msgField2, value);
                PipeWriter.publishWrites(pinOutput);
                            
                PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220);
                PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_CONNECTOR_111, mask|port.port);
                PipeWriter.writeLong(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTION_220_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
                
                PipeWriter.publishWrites(pinOutput);
                
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
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_BLOCKCHANNEL_22)) {

                PipeWriter.writeLong(pinOutput, GroveRequestSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
                PipeWriter.publishWrites(pinOutput);
                
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
            if (PipeWriter.hasRoomForWrite(goPipe) &&  PipeWriter.tryWriteFragment(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221)) {

                PipeWriter.writeInt(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_CONNECTOR_111, port.port);
                PipeWriter.writeLong(pinOutput, GroveRequestSchema.MSG_BLOCKCONNECTIONUNTIL_221_FIELD_TIMEMS_114, time);
                PipeWriter.publishWrites(pinOutput);
                
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
