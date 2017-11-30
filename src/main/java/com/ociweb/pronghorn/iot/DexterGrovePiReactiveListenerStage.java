package com.ociweb.pronghorn.iot;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.impl.grovepi.GrovePiConstants;
import com.ociweb.iot.impl.AnalogListenerBase;
import com.ociweb.iot.impl.DigitalListenerBase;
import com.ociweb.iot.impl.I2CListenerBase;
import com.ociweb.iot.impl.RotaryListenerBase;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeReader;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;

public class DexterGrovePiReactiveListenerStage extends ReactiveIoTListenerStage{


	private static final Logger logger = LoggerFactory.getLogger(DexterGrovePiReactiveListenerStage.class);

	public DexterGrovePiReactiveListenerStage(GraphManager graphManager, Behavior listener, 
			                                  Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, 
			                                  ArrayList<ReactiveManagerPipeConsumer> consumers,
			                                  HardwareImpl hardware, int parallelInstance, String nameId) {
		super(graphManager, listener, inputPipes, outputPipes, consumers, hardware, parallelInstance, nameId);
	}
    
    
	@Override
	protected void processI2CMessage(Object listener, Pipe<I2CResponseSchema> p) {
		if (listener instanceof I2CListenerBase 
			|| listener instanceof DigitalListenerBase 
			|| listener instanceof AnalogListenerBase) {
			int addr = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_ADDRESS_11);
			int register = PipeReader.readInt(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_REGISTER_14);
			long time = PipeReader.readLong(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_TIME_13);

			byte[] backing = PipeReader.readBytesBackingArray(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int position = PipeReader.readBytesPosition(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int length = PipeReader.readBytesLength(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);
			int mask = PipeReader.readBytesMask(p, I2CResponseSchema.MSG_RESPONSE_10_FIELD_BYTEARRAY_12);

			
			logger.debug("Pi listener consuming I2C message from addr: {}",addr);

			if(listener instanceof DigitalListenerBase && addr==4 && length==1){
				int tempValue = backing[position&mask];

				int connector = GrovePiConstants.REGISTER_TO_PORT[register];
				assert(connector!=-1);

				commonDigitalEventProcessing(Port.DIGITALS[connector], time, tempValue, (DigitalListenerBase)listener);

			}
			else if(listener instanceof AnalogListenerBase && addr==4 && length==3){

				int high = (int)backing[(position+1)&mask];
				int low = (int)backing[(position+2)&mask];

				if (-1==high && -1==low) {

					//no data was available, we may be polling too fast

				} else {
					int tempValue =  (high<<8) | (0xFF&low);
					int connector = GrovePiConstants.REGISTER_TO_PORT[register];
					assert(connector>=0) :"bad connector "+connector;

					//The dexter grove only has 10 bit analog to digital converter so we should never find a value larger than 1024
					if ((tempValue<0) || (tempValue>1024)) {
						logger.error("connection {} bad i2c result array [{}, {}, {}] ",connector,backing[(position+0)&mask],backing[(position+1)&mask],backing[(position+2)&mask]);
					} else {						
						
						//force the range for analog input
						Port p2 = Port.ANALOGS[connector];
						int range = builder.getConnectedDevice(p2).range();
						
						int finalValue = (range*tempValue)>>10;// divides by 1024;
					
						commonAnalogEventProcessing(p2, time, finalValue, (AnalogListenerBase)listener);
					}
				}
			}else if(listener instanceof RotaryListenerBase && addr==4 && length==2){
				byte[] tempArray = {backing[(position+0)&mask], backing[(position+1)&mask]};
				int tempValue = (((int)tempArray[0])<<8) | (0xFF&((int)tempArray[1]));
				((RotaryListenerBase)listener).rotaryEvent(Port.DIGITALS[register], time, tempValue, 0, 0);
			} else if (listener instanceof I2CListenerBase){ //must be last so we only do this if one of the more specific conditions were not met first.
				super.commonI2CEventProcessing((I2CListenerBase)listener, addr, register, time, backing, position, length, mask);;
				logger.debug("Creating I2C event");
			}
		}
	}
}

