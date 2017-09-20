package com.ociweb.grove;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D3;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port LED_PORT    = D2;
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, BUTTON_PORT);
        c.connect(LED, LED_PORT);
    }
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        //this digital listener will get all the button press and un-press events
        runtime.addDigitalListener(new LEDBehavior(runtime));
        
    }
}
