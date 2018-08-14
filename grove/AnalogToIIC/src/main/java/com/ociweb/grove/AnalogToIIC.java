package com.ociweb.grove;

import static com.ociweb.iot.grove.adc.ADCTwig.*;

import com.ociweb.iot.maker.*;

public class AnalogToIIC implements FogApp
{

    @Override
    public void declareConnections(Hardware c) {

        c.connect(ADC.ReadConversionResult);
        c.connect(ADC.ReadAlertStatus);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AnalogToIICBehavior(runtime));

    }
          
}
