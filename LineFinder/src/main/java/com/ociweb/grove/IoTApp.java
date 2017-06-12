package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;


public class IoTApp implements IoTSetup {
           
	public static Port LED_PORT = D4;
        public static Port LineFinderPort = D3;
        private final int LED_HIGH = LED.range()-1;       
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {        
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LineFinderPort);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value*LED_HIGH);
                System.out.println("In/Out of Line");
                    	        	
        });
              
    }
    
}