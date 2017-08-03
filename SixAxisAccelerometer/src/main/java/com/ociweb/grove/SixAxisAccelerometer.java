package com.ociweb.grove;


import com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometerTwig;
import com.ociweb.iot.maker.*;

public class SixAxisAccelerometer implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {

        c.connect(SixAxisAccelerometerTwig.SixAxisAccelerometer.readAccel);
        c.connect(SixAxisAccelerometerTwig.SixAxisAccelerometer.readMag);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener(new AccelBehavior(runtime));
    }
          
}
