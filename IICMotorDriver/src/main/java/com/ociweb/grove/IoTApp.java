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
        ////////////////////////////
        //Connection specifications
        ///////////////////////////        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.
        c.useI2C();
        c.connect(MotorDriver);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        runtime.registerListener(new MotorDriverBehavior(runtime));
    }
}
