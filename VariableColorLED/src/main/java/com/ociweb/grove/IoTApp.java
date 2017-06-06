package com.ociweb.grove;



import static com.ociweb.iot.grove.GroveTwig.AngleSensor;
import static com.ociweb.iot.grove.GroveTwig.LightSensor;
import static com.ociweb.iot.maker.Port.A1;
import static com.ociweb.iot.maker.Port.A2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.TimeTrigger;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;

/**
 * As it gets dark the back light of the LCD comes on.
 * Angle sensor is used for brightness adjustment
 */

public class IoTApp implements IoTSetup
{

	public static final Port LIGHT_SENSOR_PORT = A2;
	public static final Port ANGLE_SENSOR_PORT = A1;
	    
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
    	
    	c.connect(LightSensor, LIGHT_SENSOR_PORT);
    	c.connect(AngleSensor, ANGLE_SENSOR_PORT);
    	c.useI2C();
    	c.setTriggerRate(TimeTrigger.OnTheSecond);
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
            	    	
    	final CommandChannel rgbLightChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addAnalogListener((port, time, durationMillis, average, value)->{    	    
    		switch(port) {
	    		case A2://LIGHT_SENSOR_PORT
	    			
	    			int leadingZeros =  Integer.numberOfLeadingZeros(value)- (32-10); //value is only 10 bits max

	    			int level = Math.min(255, (brightness * Math.min(leadingZeros,8))/8);
	    			Grove_LCD_RGB.commandForColor(rgbLightChannel, level, level, level);	    			
	    				    			
	    			break;
	    		
	    		case A1://ANGLE_SENSOR_PORT
	    			
	    			brightness = ((AngleSensor.range()/2) * value)/AngleSensor.range();	    			
	    				    			
	    			break;
    		}
    		
    		
    	});
    	

    	final CommandChannel lcdTextChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addTimeListener((time)->{ 
    		
    		  LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
    		
    		  String text = date.format(formatter1)+"\n"+date.format(formatter2);

			  Grove_LCD_RGB.commandForText(lcdTextChannel, text);
    		
    	});
    	
    }
        
  
}
