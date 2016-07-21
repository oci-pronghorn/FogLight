package com.ociweb.iot.metronome;


import static com.ociweb.iot.grove.GroveTwig.Buzzer;
import static com.ociweb.iot.grove.GroveTwig.Relay;
import static com.ociweb.iot.grove.GroveTwig.LED;

import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.AngleSensor;

import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup {
    
    public static final int ROTARY_ANGLE_CONNECTION = 14;
    public static final int BUZZER_CONNECTION = 2;    
    public static final int BUTTON_CONNECTION = 4;    
    
        
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        c.useConnectD(Buzzer, BUZZER_CONNECTION); //could use relay or LED instead of buzzer if desired
        c.useConnectD(Button, BUTTON_CONNECTION);
        c.useConnectA(AngleSensor, ROTARY_ANGLE_CONNECTION);
        c.useI2C();
    }


    @Override
    public void declareBehavior(IOTDeviceRuntime runtime) {
        runtime.registerListener(new MetronomeBehavior(runtime));
    }        
  
}
