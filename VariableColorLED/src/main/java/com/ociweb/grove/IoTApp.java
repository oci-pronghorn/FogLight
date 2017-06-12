package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import java.lang.*;

import com.ociweb.gl.api.TimeTrigger;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;
import java.util.logging.Level;
import java.util.logging.Logger;


public class IoTApp implements IoTSetup {
           
	public static Port LED_PORT = D3;
        private int lightIntensity = 0;
        private boolean brighter = true;
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.setTriggerRate(50);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
           
        runtime.addTimeListener((time)->{
            if (lightIntensity == 0 || brighter){                
                lightIntensity += 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == LED.range()-1){
                    brighter = false;
                }
                System.out.println("going up "+lightIntensity);
            }else{
                lightIntensity -= 1;
                ledChannel.setValue(LED_PORT, lightIntensity);
                if(lightIntensity == 0){
                    brighter = true;
                }
                System.out.println("going down "+lightIntensity);
            }            
            });            
    }
    
}

