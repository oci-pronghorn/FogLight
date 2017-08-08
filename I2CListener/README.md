# What you will need before you start:
-[Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) 

-[Maven](https://maven.apache.org/install.html), which downloads and manages the libraries and APIs needed to get the Grove device working.

-[Git](https://git-scm.com/), which clones a template Maven project with the necessary dependencies already set up.

# Starting your Maven project: 
[Starting a mvn project](https://github.com/oci-pronghorn/FogLighter/blob/master/README.md)

# Example project:

The following sketch will demonstrate a basic demo for using a ```I2CListener()```.

Demo code:

```
package com.coiweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.adc.ADCTwig.ADC;

public class I2CListener implements FogApp
{



    @Override
    public void declareConnections(Hardware c) {
        
    	c.useI2C();
    	c.connect(ADC.ReadConversionResult);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	runtime.addI2CListener(new I2CListenerBehavrio(runtime));

    }
          
}

```
Behavior class:

```
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

```
This class is a simple demonstration of how to use the ```I2CListener()```. This demonstration uses the I2C ADC, which is an analog to I2C converter. Also, take note that inside of the I2CListenerBehavior, there is also a startup listener which is necessary to begin an I2CListener. 
