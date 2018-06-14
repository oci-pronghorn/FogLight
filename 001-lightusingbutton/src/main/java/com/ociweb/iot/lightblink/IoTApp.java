package com.ociweb.iot.lightblink;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D4;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;


public class IoTApp implements FogApp {
           
	public static Port LED_PORT = D4;
	
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(Button, D3);        
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {
        
        final FogCommandChannel ledChannel = runtime.newCommandChannel(
        		GreenCommandChannel.DYNAMIC_MESSAGING | FogRuntime.PIN_WRITER); 
        
        runtime.addDigitalListener((connection,time,durationMillis, value)->{
        	
            ledChannel.setValue(LED_PORT,value);
        	        	
        });
              
    }  
}

