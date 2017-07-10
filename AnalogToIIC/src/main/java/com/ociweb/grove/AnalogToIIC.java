package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import static com.ociweb.iot.grove.adc.ADCTwig.*;

import com.ociweb.iot.maker.*;

public class AnalogToIIC implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////


    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        c.useI2C();
        c.connect(ADC.ReadConversionResult,500);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AnalogToIICBehavior(runtime));

    }
          
}
