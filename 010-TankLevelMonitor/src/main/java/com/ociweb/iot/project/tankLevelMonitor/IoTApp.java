package com.ociweb.iot.project.tankLevelMonitor;


import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.UltrasonicRanger;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{

    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        c.connect(Button, D2);
        c.connect(UltrasonicRanger, A0);
        
        
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
    	 runtime.registerListener(new IoTBehavior(runtime));
    }
        
  
}
