package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.D5;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp {
    
    private static final int PAUSE = 500;
           
    public static final Port LED_PORT = D5;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
        c.setTimerPulseRate(PAUSE*2);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel blinkerChannel = runtime.newCommandChannel( FogRuntime.PIN_WRITER); 
        
        runtime.addTimePulseListener((time,instance)->{
        	
        	blinkerChannel.setValueAndBlock(LED_PORT, true, PAUSE);
        	blinkerChannel.setValue(LED_PORT, false);
        	        	
        });
              
    }  
}
