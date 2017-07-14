package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.MsgCommandChannel;
import com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig;
import com.ociweb.iot.grove.four_digit_display.FourDigitDisplayCommand;
import com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.pipe.PipeWriter;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiCommandChannel extends FogCommandChannel{

	private final byte groveAddr = 0x04;

	private Logger logger = LoggerFactory.getLogger(PiCommandChannel.class);



	public PiCommandChannel(GraphManager gm, HardwareImpl hardware, int features, 
			               int instance, PipeConfigManager pcm, 
			               byte commandIndex) { 
				
		super(gm, hardware, features |  ((features&FogRuntime.PIN_WRITER)==0?0:FogRuntime.I2C_WRITER), instance, pcm); 
	}

	@Override
	public boolean block(Port port, long duration) { 
		return block((port.isAnalog()?ANALOG_BIT:0)|port.port,duration); 
	}

	private boolean block(int connector, long duration) { 

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {            

			if (goHasRoom() && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20)) {

				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_CONNECTOR_11, connector);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_ADDRESS_12, groveAddr);
				PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTION_20_FIELD_DURATIONNANOS_13, duration*MS_TO_NS);

				PipeWriter.publishWrites(i2cOutput);

				MsgCommandChannel.publishGo(1,HardwareImpl.i2cIndex(builder),this);
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

			if (goHasRoom() && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21)) {

				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_CONNECTOR_11, port.port);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_ADDRESS_12, groveAddr);
				PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCONNECTIONUNTIL_21_FIELD_TIMEMS_14, time);

				PipeWriter.publishWrites(i2cOutput);

				MsgCommandChannel.publishGo(1,HardwareImpl.i2cIndex(builder),this);
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
					goHasRoom() ) {

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

				MsgCommandChannel.publishGo(msgCount,HardwareImpl.i2cIndex(builder),this);

				return true;
			}else{
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	@Override
	public boolean setValue(Port port, boolean value) {
		return setValue(port, (!value) ? 0 : builder.getConnectedDevice(port).range()-1);
	}

	//Build templates like this once that can be populated and sent without redefining the part that is always the same.
	private final byte[] digitalMessageTemplate = {0x01, 0x02, -1, -1, 0x00};
	private final byte[] analogMessageTemplate = {0x01, 0x04, -1, -1, 0x00};

	private boolean setFourDigitDisplayValue(Port port, int value){
		switch (value){
			case FourDigitDisplayCommand.INIT:
				return  Grove_FourDigitDisplay.init(this, port);
			
			case FourDigitDisplayCommand.DISPLAY_ON:
				return  Grove_FourDigitDisplay.displayOn(this, port);
			
			case FourDigitDisplayCommand.DISPLAY_OFF:
				return Grove_FourDigitDisplay.displayOff(this, port);
				
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 0:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 1:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 2:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 3:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 4:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 5:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 6:
			case FourDigitDisplayCommand.SET_BRIGHTNESS + 7:
				return Grove_FourDigitDisplay.setBrightness(this, port, value - FourDigitDisplayCommand.SET_BRIGHTNESS);
			
			default:
				return Grove_FourDigitDisplay.printFourDigitsWithColon(this, port, value / 100, value % 100);
			
			}
	}

	public boolean setValue(Port port, int value) {

		IODevice connectedDevice = builder.getConnectedDevice(port);

		if (connectedDevice == AnalogDigitalTwig.FourDigitDisplay){
			return setFourDigitDisplayValue(port, value);
		}


		int mask = port.isAnalog()? ANALOG_BIT:0;

		boolean isPWM = connectedDevice.isPWM();

		byte[] template = port.isAnalog() | isPWM ? analogMessageTemplate : digitalMessageTemplate;

		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			if (goHasRoom() && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_COMMAND_7)) { 

				template[2] = (byte)port.port;
				template[3] = (byte)value;	

				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_CONNECTOR_11, mask|port.port);
				PipeWriter.writeInt(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_ADDRESS_12, groveAddr);                
				PipeWriter.writeBytes(i2cOutput, I2CCommandSchema.MSG_COMMAND_7_FIELD_BYTEARRAY_2, template);


				logger.debug("CommandChannel sends analogWrite i2c message");
				PipeWriter.publishWrites(i2cOutput);

				MsgCommandChannel.publishGo(1,HardwareImpl.i2cIndex(builder),this);				
				return true;
			}else{
				return false;
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}

	@Override
	public boolean setValueAndBlock(Port port, boolean value, long durationMilli) {
		return setValueAndBlock(port, 
				(!value) ? 0 : builder.getConnectedDevice(port).range()-1,
						durationMilli);
	}

	@Override
	public boolean setValueAndBlock(Port port, int value, long msDuration) {
		assert(enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			int mask = port.isAnalog() ? ANALOG_BIT : 0;
			boolean isPWM = builder.getConnectedDevice(port).isPWM();
			byte[] template = port.isAnalog()|isPWM? analogMessageTemplate : digitalMessageTemplate;


			if (goHasRoom() && PipeWriter.hasRoomForFragmentOfSize(i2cOutput, 
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

				MsgCommandChannel.publishGo(2,HardwareImpl.i2cIndex(builder),this);

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

			if (goHasRoom() && PipeWriter.tryWriteFragment(i2cOutput, I2CCommandSchema.MSG_BLOCKCHANNEL_22)) {

				PipeWriter.writeLong(i2cOutput, I2CCommandSchema.MSG_BLOCKCHANNEL_22_FIELD_DURATIONNANOS_13, msDuration*MS_TO_NS);
				PipeWriter.publishWrites(i2cOutput);

				MsgCommandChannel.publishGo(1,HardwareImpl.i2cIndex(builder),this);
				return true;
			} else {              
				return false; 
			}

		} finally {
			assert(exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}




}
