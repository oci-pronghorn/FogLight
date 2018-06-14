package com.ociweb.iot.track1;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.AngleSensor;
import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.LightSensor;
import static com.ociweb.iot.maker.Port.A1;
import static com.ociweb.iot.maker.Port.A2;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.lcd_rgb.Grove_LCD_RGB;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

/**
 * As it gets dark the back light of the LCD comes on.
 * Angle sensor is used for brightness adjustment
 */

public class IoTApp implements FogApp
{
	public static final Port LIGHT_SENSOR_PORT = A2;
	public static final Port ANGLE_SENSOR_PORT = A1;
	    
	int brightness = 255;
	
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
    	
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
    	c.connect(AngleSensor, ANGLE_SENSOR_PORT);
    	c.useI2C();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
           	
    	
    	final FogCommandChannel lcdScreenChannel = runtime.newCommandChannel(
    			GreenCommandChannel.DYNAMIC_MESSAGING | FogRuntime.I2C_WRITER );
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
