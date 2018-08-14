package com.ociweb.grove;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Buzzer;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D8;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port BUZZER_PORT = D8;

    @Override
    public void declareConnections(Hardware c) {
        c.connect(Button, BUTTON_PORT); 
        c.connect(Buzzer, BUZZER_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
  
        runtime.addDigitalListener(new BuzzerBehavior(runtime));
       
    }
}
