package com.ociweb.grove;


import com.ociweb.iot.maker.*;

import static com.ociweb.iot.grove.analogdigital.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class LCDRGB implements FogApp
{
   


    @Override
    public void declareConnections(Hardware c) {
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new LCDRGB_Behavior(runtime));
    }
          
}
