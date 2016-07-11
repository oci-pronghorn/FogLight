package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.RestListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.iot.maker.StartupListener;
import com.ociweb.iot.maker.TimeListener;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.pipe.RawDataSchema;
import com.ociweb.pronghorn.stage.PronghornStage;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiReactiveListenerStage extends ReactiveListenerStage{


	public PiReactiveListenerStage(GraphManager graphManager, Object listener, Pipe[] pipes) {
		super(graphManager, listener, pipes);             
	}

	@Override
	protected void consumeI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {
		
		while (PipeReader.tryReadFragment(p)) {                

			int msgIdx = PipeReader.getMsgIdx(p);
			switch (msgIdx) {   
			case I2CResponseSchema.MSG_RESPONSE_10:
				if (listener instanceof I2CListener) {
					int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
					int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
					int time = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

					byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);


					((I2CListener)listener).i2cEvent(addr, register, time, backing, position, length, mask);

				}
				else if (listener instanceof DigitalListener){
					int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
					int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
					int time = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

					byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);

					assert(addr == 4);
					assert(length == 1);

					System.out.println("received grove digital signal " + backing[position&mask]);
					((DigitalListener)listener).digitalEvent(register, time, backing[position&mask]);
				}
				else if (listener instanceof AnalogListener){
					int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
					int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
					int time = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

					byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);

					assert(addr == 4);
					assert(length == 3);

					byte[] temp = Arrays.copyOfRange(backing, position&mask, (position+3)&mask); //TODO: Does this produce garbage?
							((AnalogListener)listener).analogEvent(register, time, 0, temp[1]*256+((int)temp[2]&0xFF)); //TODO: Average=?
				}
				break;
			case -1:

				requestShutdown();
				PipeReader.releaseReadLock(p);
				return;

			default:
				throw new UnsupportedOperationException("Unknown id: "+msgIdx);

			}
			//done reading message off pipe
			PipeReader.releaseReadLock(p);
		}
	} 
}
