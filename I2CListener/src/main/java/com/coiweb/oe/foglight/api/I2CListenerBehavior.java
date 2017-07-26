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
	public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {

		if(addr == ADDR_ADC121 && register == REG_ADDR_RESULT){
			 System.out.println(sensor.interpretData(backing, position, length, mask));   
	     }
	}
	@Override
	public void startup() {
       sensor.begin();
		
	}

}
