package com.ociweb.grove;


import static com.ociweb.iot.grove.motor_driver.MotorDriverTwig.MotorDriver;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;

import com.ociweb.iot.maker.Hardware;


public class IoTApp implements FogApp
{
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(MotorDriver);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener(new MotorDriverBehavior(runtime));
    }
}
