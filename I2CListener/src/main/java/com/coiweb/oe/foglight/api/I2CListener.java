package com.coiweb.oe.foglight.api;

import com.ociweb.iot.maker.*;
import com.ociweb.iot.grove.adc.ADCTwig.ADC;

public class I2CListener implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {
    	c.connect(ADC.ReadConversionResult);
    }
    @Override
    public void declareBehavior(FogRuntime runtime) {   
    	runtime.registerListener(new I2CListenerBehavior(runtime));
    }
}
