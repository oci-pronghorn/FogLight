package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class GPS implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {
    	 c.useSerial(Baud.B_____9600);
    	 c.limitThreads();
    	 
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.addBehavior(new GPSBehavior(runtime));
    }
          
}
