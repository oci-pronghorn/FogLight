package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;
import static com.ociweb.iot.grove.GroveTwig.AngleSensor;

import com.ociweb.gl.api.GreenCommandChannel;

public class IoTApp implements IoTSetup
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
    // // by using constants such as these you can easily use the right value to reference where the sensor was plugged in
      
    //private static final Port BUTTON_PORT = D3;
    //private static final Port LED_PORT    = D2;
    //private static final Port RELAY_PORT  = D7;
    private static final Port LIGHT_SENSOR_PORT= A2;
    
    public int brightness = 255;
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        //c.connect(Button, BUTTON_PORT); 
        //c.connect(Relay, RELAY_PORT);         
        c.connect(LightSensor, LIGHT_SENSOR_PORT); 
        //c.connect(LED, LED_PORT);        
        c.useI2C();
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        //  //Use lambdas or classes and add listeners to the runtime object
        //  //CommandChannels are created to send outgoing events to the hardware
        //  //CommandChannels must never be shared between two lambdas or classes.
        //  //A single lambda or class can use mulitiple CommandChannels for cuoncurrent behavior
        
        
    	final CommandChannel lcdScreenChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addAnalogListener((port, time, durationMillis, average, value)->{
     		
	    			
    		lcdScreenChannel.setValue(LIGHT_SENSOR_PORT, value);

    		System.out.println(value);
	    		   		
    		
    	});
    }
        
  
}
