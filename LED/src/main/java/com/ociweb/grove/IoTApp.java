package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements FogApp
{
    private static final Port BUTTON_PORT = D3;
    private static final Port LED_PORT    = D2;
    
    @Override
    public void declareConnections(Hardware c) {
              
        c.connect(Button, BUTTON_PORT); 
        c.connect(LED, LED_PORT);        
        c.useI2C();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
           
    	final FogCommandChannel channel1 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
        //this digital listener will get all the button press and un-press events 
        runtime.addDigitalListener((port, connection, time, value)->{
        	if (BUTTON_PORT == port){
                	channel1.setValueAndBlock(LED_PORT, value, 200); 
                }
        });
    }
}
