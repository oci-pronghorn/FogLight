package com.ociweb.iot.hz;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup
{

    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(UltrasonicRanger, A0);
    	
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
            	
    	runtime.registerListener(new PhysicalWatcher(runtime) );
    	
    	runtime.registerListener(new AlertTrigger(runtime, D7) );
    	    	
    }
        
  
}
