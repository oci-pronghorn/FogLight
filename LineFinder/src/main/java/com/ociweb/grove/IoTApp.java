package com.ociweb.grove;

import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;

import static com.ociweb.iot.maker.Port.*;



public class IoTApp implements FogApp {
           
	private static final Port LED_PORT = D4;
        private static final Port LINEFINDER_PORT = D3;
        
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {        
        hardware.connect(LED, LED_PORT);
        hardware.connect(LineFinder, LINEFINDER_PORT);        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel ledChannel = runtime.newCommandChannel(DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
                ledChannel.setValue(LED_PORT,value==1);
                System.out.println("In/Out of Line");                
        });
              
    }
    
}