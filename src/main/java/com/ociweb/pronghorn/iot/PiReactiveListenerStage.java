package com.ociweb.pronghorn.iot;

import java.util.Arrays;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class PiReactiveListenerStage extends ReactiveListenerStage{

	private int lastDigital = -1;
	private int lastAnalog = -1;
	
	
	public PiReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
		super(graphManager, listener, inputPipes, outputPipes);             
	}

	@Override
	protected void consumeI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {
		while (PipeReader.tryReadFragment(p)) {                
			int msgIdx = PipeReader.getMsgIdx(p);
			switch (msgIdx) {   
			case I2CResponseSchema.MSG_RESPONSE_10:
				if (listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener) {
					int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
					int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
					long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

					byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
					int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);

					
					if (listener instanceof I2CListener){
						((I2CListener)listener).i2cEvent(addr, register, time, backing, position, length, mask);
					}
					else if(listener instanceof DigitalListener && addr==4 && length==1){
						int tempValue = backing[position&mask];
						if(tempValue!=lastDigital){
							lastDigital = tempValue;
							((DigitalListener)listener).digitalEvent(register, time, tempValue);
						}
					}
					else if(listener instanceof AnalogListener && addr==4 && length==3){
						byte[] temp = Arrays.copyOfRange(backing, position&mask, (position+3)&mask); //TODO: Does this produce garbage?
						int tempValue = ((int)temp[1])*256+(((int)temp[2])&0xFF);
						if(tempValue!=lastAnalog){
							lastAnalog = tempValue;
							((AnalogListener)listener).analogEvent(register, time, 0, tempValue); //TODO: Average=?
						}
					}
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
