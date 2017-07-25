package com.coiweb.oe.foglight.api;

import static com.ociweb.iot.grove.adc.ADC_Constants.*;
import com.ociweb.iot.grove.adc.*;

import com.ociweb.iot.grove.adc.*;

import static com.ociweb.iot.maker.FogRuntime.I2C_WRITER;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

public class I2CListenerBehavrio implements I2CListener, StartupListener {
	private final FogCommandChannel ch;
    private final ADC_Transducer sensor;
        
	public I2CListenerBehavrio(FogRuntime runtime) {

		this.ch = runtime.newCommandChannel(I2C_WRITER);
        sensor = new ADC_Transducer(ch);
	}

	@Override
	public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {

		if(addr == ADDR_ADC121 && register == REG_ADDR_RESULT){
			 System.out.println(sensor.interpretData(backing, position, length, mask));   
	     }
	}
	@Override
	public void startup() {
       sensor.writeSingleByteToRegister(REG_ADDR_CONFIG,0x20);
       sensor.begin();
		
	}

}
