package com.coiweb.oe.foglight.api;

import static com.ociweb.iot.grove.adc.ADC_Constants.*;
import com.ociweb.iot.grove.adc.*;
import static com.ociweb.iot.maker.FogRuntime.I2C_WRITER;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

public class I2CListenerBehavior implements I2CListener, StartupListener {

    private final ADC_Transducer sensor;
        
	public I2CListenerBehavior(FogRuntime runtime) {
        sensor = new ADC_Transducer(runtime.newCommandChannel());

	}
	@Override
	public void startup() {
       		sensor.begin();		
	}
	@Override
	public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
		// i2cEvent triggers when there's data from an i2c read of a device
		// addr is the i2c address of the device
		// register is the register address of the data
		// backing is a circular buffer, with size = mask containing bytes read from i2c
		// position is the index of the first byte of the i2c data read event
		// length is the number of bytes of i2c data read event
		if(addr == ADDR_ADC121 && register == REG_ADDR_RESULT){
			short temp = (short)(((backing[(position)&mask]&0x0F) << 8) | (backing[(position+1)&mask]&0xFF));
			System.out.println("The conversion reading is: "+ temp);   
	     }
	}

}
