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
