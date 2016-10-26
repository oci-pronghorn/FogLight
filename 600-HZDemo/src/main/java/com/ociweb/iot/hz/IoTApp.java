package com.ociweb.iot.hz;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTApp implements IoTSetup
{
	
	private final Port BUTTON_PORT = D7;
	private final Port LED_PORT    = D3;
	
	private final Port ANGLE_PORT  = A0;
	private final Port SONIC_PORT  = A2;
	
	public final String DISPLAY_TOPIC = "display";
	
	private final static Logger logger = LoggerFactory.getLogger(IoTApp.class);

    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(UltrasonicRanger, SONIC_PORT);
    	c.connect(Button, BUTTON_PORT);
    	//c.connect(AngleSensor, ANGLE_PORT);
    	c.connect(LED, LED_PORT);
    	
    	c.useI2C();
    	c.setTriggerRate(200);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
            	
    	runtime.registerListener(new PhysicalWatcher(runtime, DISPLAY_TOPIC) ).includePorts(SONIC_PORT, BUTTON_PORT);
    	
    	runtime.registerListener(new AlertTrigger(runtime, LED_PORT, DISPLAY_TOPIC) );
    	    	
    	runtime.registerListener(new Display(runtime)).addSubscription(DISPLAY_TOPIC);
    	
    }
        
  
}
