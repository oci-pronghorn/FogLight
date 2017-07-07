package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.grove.adc

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

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
        c.connect(I2C_ADCTwig.ReadConversionResult,500);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AnalogToIICBehavior(runtime));

    }
          
}
