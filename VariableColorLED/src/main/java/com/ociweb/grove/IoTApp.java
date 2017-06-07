package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.GreenCommandChannel;


public class IoTApp implements IoTSetup {
           
	public static Port GREENLED_PORT = D4;
        public static Port BLUELED_PORT = D7;
        public static Port PIR_Sensor = D3;
        public static Port ClickButton = D2;
               
        
    public static void main( String[] args) {
        DeviceRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware hardware) {
        hardware.connect(LED, GREENLED_PORT);
        hardware.connect(LED,BLUELED_PORT);
        hardware.connect(Button, PIR_Sensor);
        hardware.connect(Button,ClickButton);
    }

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
        final CommandChannel ledChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING); 
        runtime.addDigitalListener((port,time,durationMillis, value)->{
            if (port == PIR_Sensor){
                ledChannel.setValue(GREENLED_PORT,value);
                System.out.println("PIR_Sensor");
            }else if (port==ClickButton) {
                System.out.println("Button");
                ledChannel.setValue(BLUELED_PORT,value); 
            }

        	        	
        });
              
    }
    
}

