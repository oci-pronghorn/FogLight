package com.ociweb.iot.nightlight; //TODO: namespace needs to remain the same since we are bulding on previous work.  Better NAME?


import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.LightSensor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
	    
	private int brightness = 255;
	
	    
    private final DateTimeFormatter formatter1;
    private final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EE MMM dd,yyyy");
    private final ZoneId zone = ZoneId.systemDefault();
    
    //TODO: NOTE: the pi may have the wrong time and should be set.
    //           MMDDhhmmYY
    // sudo date 0728224916
    // may need to set timezone:   sudo dpkg-reconfigure tzdata
    // may need to install NTP:   sudo apt-get install ntpdate
    
	public IoTApp(boolean is24HourTime) {
	
	    if (is24HourTime) {
	        formatter1 = DateTimeFormatter.ofPattern("  HH:mm:ss");	        
	    } else {
	        formatter1 = DateTimeFormatter.ofPattern("  hh:mm:ss a");
	    }
	    
	}
	
    public static void main( String[] args ) {
        
        boolean is24HourTime = args.length>0 && "24".equals(args[0]);
                
        DeviceRuntime.run(new IoTApp(is24HourTime));
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
    	
    	c.useConnectA(LightSensor, LIGHT_SENSOR_CONNECTION, 100);
    	c.useConnectA(AngleSensor, ANGLE_SENSOR_CONNECTION);
    	c.useI2C();
    	c.useTriggerRate(1000);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
            	    	
    	final CommandChannel rgbLightChannel = runtime.newCommandChannel();
    	runtime.addAnalogListener((connection, time, average, value)->{    	    
    		switch(connection) {
	    		case LIGHT_SENSOR_CONNECTION:
	    			
	    			int leadingZeros =  Integer.numberOfLeadingZeros(value)- (32-10); //value is only 10 bits max

	    			int level = Math.min(255, (brightness * Math.min(leadingZeros,8))/8);
	    			Grove_LCD_RGB.commandForColor(rgbLightChannel, level, level, level);	    			
	    				    			
	    			break;
	    		
	    		case ANGLE_SENSOR_CONNECTION:
	    			
	    			brightness = ((AngleSensor.range()/2) * value)/AngleSensor.range();	    			
	    				    			
	    			break;
    		}
    		
    		
    	});
    	

    	final CommandChannel lcdTextChannel = runtime.newCommandChannel();
    	runtime.addTimeListener((time)->{ 
    		
    		  LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
    		
    		  String text = date.format(formatter1)+"\n"+date.format(formatter2);

			  Grove_LCD_RGB.commandForText(lcdTextChannel, text);
    		
    	});
    	
    }
        
  
}
