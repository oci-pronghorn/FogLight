package com.ociweb.pronghorn.iot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DexterGrovePiReactiveListenerStage extends ReactiveListenerStage{

	private int lastDigital = -1;
	private int lastAnalog = -1;

	
	private static final Logger logger = LoggerFactory.getLogger(DexterGrovePiReactiveListenerStage.class);
	
	public DexterGrovePiReactiveListenerStage(GraphManager graphManager, Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, Hardware hardware) {
		super(graphManager, listener, inputPipes, outputPipes, hardware); 

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
					
					logger.debug("Pi listener consuming I2C message");

					if (listener instanceof I2CListener){
						((I2CListener)listener).i2cEvent(addr, register, time, backing, position, length, mask);
						logger.debug("Creating I2C event");
					}
					else if(listener instanceof DigitalListener && addr==4 && length==1){
						int tempValue = backing[position&mask];
						if(tempValue!=lastDigital){
							lastDigital = tempValue;
							register = GrovePiConstants.REGISTER_TO_PIN[register];
							assert(register!=-1);
							((DigitalListener)listener).digitalEvent(register, time, tempValue);
							logger.debug("Digital event");
						}
					}
					else if(listener instanceof AnalogListener && addr==4 && length==3){
	
						int high = (int)backing[(position+1)&mask];
                        int low = (int)backing[(position+2)&mask];
                        
                        if (-1==high && -1==low) {
                            
                            //no data was available, we may be polling too fast
                            
                        } else {
                            int tempValue =  (high<<8) | (0xFF&low);
                                
    						if (tempValue<0) {
    						    System.out.println("bad array "+backing[(position+0)&mask]+" "+backing[(position+1)&mask]+" "+backing[(position+2)&mask]);
    						} else {
    							register = GrovePiConstants.REGISTER_TO_PIN[register];
    							((AnalogListener)listener).analogEvent(register, time, 0, tempValue); //TODO: Average=? only clear after we control the poll rate
        						lastAnalog = tempValue;        			
    						}
                        }
					}else if(listener instanceof RotaryListener && addr==4 && length==2){
						byte[] tempArray = {backing[(position+0)&mask], backing[(position+1)&mask]};
						int tempValue = (((int)tempArray[0])<<8) | (0xFF&((int)tempArray[1]));
						((RotaryListener)listener).rotaryEvent(register, time, tempValue, 0, 0);
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
