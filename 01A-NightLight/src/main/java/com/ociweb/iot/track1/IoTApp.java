package com.ociweb.iot.track1;


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.LightSensor;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

/**
 * As it gets dark the back light of the LCD comes on.
 * Angle sensor is used for brightness adjustment
 */

public class IoTApp implements IoTSetup
{
	public static final Port LIGHT_SENSOR_PORT = A2;
	public static final Port ANGLE_SENSOR_PORT = A1;
	    
	int brightness = 255;
	
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
    	
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
    	c.connect(AngleSensor, ANGLE_SENSOR_PORT);
    	c.useI2C();
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
    	
    	
    	final CommandChannel lcdScreenChannel = runtime.newCommandChannel();
    	runtime.addAnalogListener((port, time, durationMillis, average, value)->{
 
    		switch(port) {
	    		case A2: assert(port == LIGHT_SENSOR_PORT);
	    			
	    			int leadingZeros =  Integer.numberOfLeadingZeros(value)- (32-10); //value is only 10 bits max

	    			int level = Math.min(255, (brightness * Math.min(leadingZeros,8))/8);

	    			Grove_LCD_RGB.commandForColor(lcdScreenChannel, level, level, level);	    			
	    				    			
	    			break;
	    		
	    		case A1: assert(port == ANGLE_SENSOR_PORT);
	    			
	    		    brightness = ((AngleSensor.range()/2) * value)/AngleSensor.range();    	
	    			
	    			break;
	    		
    		
    		}
    		
    		
    	});
    	
    }
        
  
}
