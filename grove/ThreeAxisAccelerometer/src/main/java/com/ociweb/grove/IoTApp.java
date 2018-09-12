package com.ociweb.grove;


import static com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.*;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 
    @Override
    public void declareConnections(Hardware c) {
        c.enableTelemetry();
        c.connect(ThreeAxisAccelerometer_16g.GetXYZ);
        c.connect(ThreeAxisAccelerometer_16g.GetInterrupt);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {

        runtime.registerListener(new AccelerometerBehavior(runtime));
    }
}
