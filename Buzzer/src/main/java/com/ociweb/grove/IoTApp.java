package com.ociweb.grove;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;
import static com.ociweb.iot.maker.Port.*;

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
