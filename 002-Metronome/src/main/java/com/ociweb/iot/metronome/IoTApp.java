package com.ociweb.iot.metronome;


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.Buzzer;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup {
    
    public static final Port ROTARY_ANGLE_PORT = A1;
    public static final Port BUZZER_PORT = D2;    
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(Buzzer, BUZZER_PORT); //could use relay or LED instead of buzzer if desired
        c.connect(AngleSensor, ROTARY_ANGLE_PORT, 100);
        c.useI2C();
        c.setTriggerRate(200);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        runtime.registerListener(new MetronomeBehavior(runtime));
    }        
  
}
