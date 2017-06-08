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
           
	public static Port LED_PORT = D3;
        public static Port CLICK_BUTTON = D2;
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, LED_PORT);
        hardware.connect(Button,CLICK_BUTTON);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
           
        runtime.addDigitalListener((port,time,durationMillis, value)->{
            if(port == CLICK_BUTTON){    
                ledChannel.setValue(LED_PORT,value);
                System.out.println(value);
            }
        });            
    }
}
    