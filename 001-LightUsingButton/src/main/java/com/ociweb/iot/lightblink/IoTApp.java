package com.ociweb.iot.lightblink;

import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;


public class IoTApp implements IoTSetup {
           
	public static Port LED_PORT = D4;
	
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(Button, D3);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(); 
        
        runtime.addDigitalListener((connection,time,durationMillis, value)->{
        	
            ledChannel.setValue(LED_PORT,value);
        	        	
        });
              
    }  
}

