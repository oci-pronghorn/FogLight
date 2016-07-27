package com.ociweb.iot.nightlight;


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.LightSensor;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;


/**
 * As it gets dark the back light of the LCD comes on.
 * Angle sensor is used for brightness adjustment
 */

public class IoTApp implements IoTSetup
{
	public static final int LIGHT_SENSOR_CONNECTION = 2;
	public static final int ANGLE_SENSOR_CONNECTION = 1;
	    
	int brightness = 255;
	
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
    	
    	c.useConnectA(LightSensor, LIGHT_SENSOR_CONNECTION);
    	c.useConnectA(AngleSensor, ANGLE_SENSOR_CONNECTION);
    	c.useI2C();
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        
    	
    	
    	final CommandChannel lcdScreenChannel = runtime.newCommandChannel();
    	runtime.addAnalogListener((connection, time, average, value)->{
 
    		switch(connection) {
	    		case LIGHT_SENSOR_CONNECTION:
	    			
	    			int leadingZeros =  Integer.numberOfLeadingZeros(value) - (32-10); //value is only 10 bits max

	    			int level = Math.min(255, (brightness * Math.min(leadingZeros,8))/8);

	    			Grove_LCD_RGB.commandForColor(lcdScreenChannel, level, level, level);	    			
	    				    			
	    			break;
	    		
	    		case ANGLE_SENSOR_CONNECTION:
	    			
	    			brightness = (400 * value)/1024;	    			
	    			
	    			break;
	    		
    		
    		}
    		
    		
    	});
    	
    }
        
  
}
