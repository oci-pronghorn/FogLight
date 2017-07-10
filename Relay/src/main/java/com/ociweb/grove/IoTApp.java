package com.ociweb.grove;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port RELAY_PORT  = D7;
    
    @Override
    public void declareConnections(Hardware c) {     
        c.connect(Button, BUTTON_PORT); 
        c.connect(Relay, RELAY_PORT);         
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.addDigitalListener(new RelayBehavior(runtime));
    }
}
