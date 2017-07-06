package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import static com.ociweb.iot.grove.Grove_I2C_ADC.I2C_ADC;

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
        c.connect(I2C_ADC,500);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AnalogToIICBehavior(runtime));

    }
          
}
