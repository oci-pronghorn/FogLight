package com.ociweb.iot.metronome;


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.Buzzer;

import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup {
    
    public static final int ROTARY_ANGLE_CONNECTION = 1;
    public static final int BUZZER_CONNECTION = 2;    
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connectDigital(Buzzer, BUZZER_CONNECTION); //could use relay or LED instead of buzzer if desired
        c.connectAnalog(AngleSensor, ROTARY_ANGLE_CONNECTION, 100);
        c.useI2C();
        c.setTriggerRate(200);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        runtime.registerListener(new MetronomeBehavior(runtime));
    }        
  
}
