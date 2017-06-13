package com.ociweb.iot.project.lightblink;

import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup {
    
    private static final int PAUSE = 500;
           
    public static final Port LED_PORT = D5;
    
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
        c.setTriggerRate(PAUSE*2);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel blinkerChannel = runtime.newCommandChannel(); 
        
        runtime.addTimeListener((time)->{
        	
        	blinkerChannel.setValueAndBlock(LED_PORT, true, PAUSE);
        	blinkerChannel.setValue(LED_PORT, false);
        	        	
        });
              
    }  
}
