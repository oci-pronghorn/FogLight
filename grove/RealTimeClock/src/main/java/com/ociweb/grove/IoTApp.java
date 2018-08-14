package com.ociweb.grove;


import static com.ociweb.iot.grove.real_time_clock.RTCTwig.*;
import com.ociweb.iot.hardware.impl.test.TestHardware;
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
        c.connect(RTC.ReadTime);
        c.enableTelemetry();
        if(c instanceof TestHardware){
            byte[] dummy ={0};
            ((TestHardware) c).setI2CValueToRead((byte)104,dummy,1);
        }
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new ClockBehavior(runtime));
        
    }
    
}
