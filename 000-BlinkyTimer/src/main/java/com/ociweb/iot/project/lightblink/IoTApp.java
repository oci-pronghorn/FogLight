package com.ociweb.iot.project.lightblink;

import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.LED;

import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements FogApp {
    
    private static final int PAUSE = 500;
           
    public static final Port LED_PORT = D5;
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
        c.setTriggerRate(PAUSE*2);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel blinkerChannel = runtime.newCommandChannel(); 
        
        runtime.addTimeListener((time,instance)->{
        	
        	blinkerChannel.setValueAndBlock(LED_PORT, true, PAUSE);
        	blinkerChannel.setValue(LED_PORT, false);
        	        	
        });
              
    }  
}
