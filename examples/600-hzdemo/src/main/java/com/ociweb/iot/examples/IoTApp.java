package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.UltrasonicRanger;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.LED;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.A2;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp
{
	
	private final Port BUTTON_PORT = D7;
	private final Port LED_PORT    = D3;
	
	private final Port ANGLE_PORT  = A0;
	private final Port SONIC_PORT  = A2;
	
	public final String DISPLAY_TOPIC = "display";
	
	private final static Logger logger = LoggerFactory.getLogger(IoTApp.class);

    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(UltrasonicRanger, SONIC_PORT);
    	c.connect(Button, BUTTON_PORT);
    	//c.connect(AngleSensor, ANGLE_PORT);
    	c.connect(LED, LED_PORT);
    	
    	c.useI2C();
    	c.setTimerPulseRate(200);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
            	
    	runtime.registerListener(new PhysicalWatcher(runtime, DISPLAY_TOPIC) ).includePorts(SONIC_PORT, BUTTON_PORT);
    	
    	runtime.registerListener(new AlertTrigger(runtime, LED_PORT, DISPLAY_TOPIC) );
    	    	
    	runtime.registerListener(new Display(runtime)).addSubscription(DISPLAY_TOPIC);
    	
    }
        
  
}
