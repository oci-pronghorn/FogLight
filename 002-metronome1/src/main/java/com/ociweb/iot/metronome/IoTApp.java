package com.ociweb.iot.metronome;


import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.AngleSensor;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Buzzer;
import static com.ociweb.iot.maker.Port.A1;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp {
    
    public static final Port ROTARY_ANGLE_PORT = A1;
    public static final Port BUZZER_PORT = D2;    
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Buzzer, BUZZER_PORT); //could use relay or LED instead of buzzer if desired
        c.connect(AngleSensor, ROTARY_ANGLE_PORT, 100);
        c.setTimerPulseRate(200);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        runtime.registerListener(new MetronomeBehavior(runtime));
    }        
  
}
