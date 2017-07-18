package com.ocweb.grove;


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
        c.useI2C();
        //c.connect(Button,D4);
        c.connect(ThreeAxisAccelerometer_16g.GetXYZ);
        c.connect(ThreeAxisAccelerometer_16g.GetInterrupt);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new AccelerometerBehavior(runtime));
    }
}
