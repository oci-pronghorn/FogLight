package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometerTwig;
import static com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometerTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class SixAxisAccelerometer implements FogApp
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
        c.connect(SixAxisAccelerometerTwig.SixAxisAccelerometer.getAccel);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new AccelBehavior(runtime));
    }
          
}
