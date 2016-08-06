package com.ociweb.pronghorn.iot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.hardware.HardConnection;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;
//import com.ociweb.pronghorn.util.ma.MAAvgRollerLongTest;
import com.ociweb.pronghorn.util.ma.MAvgRollerLong;

public class DexterGrovePiReactiveListenerStage extends ReactiveListenerStage{


	private static final Logger logger = LoggerFactory.getLogger(DexterGrovePiReactiveListenerStage.class);

	public DexterGrovePiReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, Hardware hardware) {
		super(graphManager, listener, inputPipes, outputPipes, hardware);
	}


	@Override
	protected void processI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {
		if (listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener) {
			int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
			int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
			long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

			byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);

			logger.debug("Pi listener consuming I2C message");

			if(listener instanceof DigitalListener && addr==4 && length==1){
				int tempValue = backing[position&mask];

				int connector = GrovePiConstants.REGISTER_TO_PIN[register];
				assert(connector!=-1);

				commonDigitalEventProcessing(connector, time,tempValue, (DigitalListener)listener);

			}
			else if(listener instanceof AnalogListener && addr==4 && length==3){

				int high = (int)backing[(position+1)&mask];
				int low = (int)backing[(position+2)&mask];

				if (-1==high && -1==low) {

					//no data was available, we may be polling too fast

				} else {
					int tempValue =  (high<<8) | (0xFF&low);
					int connector = GrovePiConstants.REGISTER_TO_PIN[register];
					assert(connector>=0) :"bad connector "+connector;

					if (tempValue<0) {
						logger.error("connection {} bad i2c result array [{}, {}, {}] ",connector,backing[(position+0)&mask],backing[(position+1)&mask],backing[(position+2)&mask]);
					} else {						
						commonAnalogEventProcessing(connector, time, tempValue, (AnalogListener)listener);
					}
				}
			}else if(listener instanceof RotaryListener && addr==4 && length==2){
				byte[] tempArray = {backing[(position+0)&mask], backing[(position+1)&mask]};
				int tempValue = (((int)tempArray[0])<<8) | (0xFF&((int)tempArray[1]));
				((RotaryListener)listener).rotaryEvent(register, time, tempValue, 0, 0);
			} else if (listener instanceof I2CListener){ //must be last so we only do this if one of the more specific conditions were not met first.
				super.commonI2CEventProcessing((I2CListener)listener, addr, register, time, backing, position, length, mask);;
				logger.debug("Creating I2C event");
			}
		}
	}
}

