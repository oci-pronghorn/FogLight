package com.ociweb.grove;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Relay;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D7;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

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
    
        runtime.addDigitalListener(new ButtonBehavior(runtime));
       
    }
}
