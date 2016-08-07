package com.ociweb.iot.stopwatch;


import static com.ociweb.iot.grove.GroveTwig.Button;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
	private static final int BUTTON_CONNECTION = 4; //long press clear, short press start/top
	
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	
	private boolean running;
	private boolean startOnUp;
	private long startTime;
	private long stopTime;
	
	private final ZoneId zone = ZoneOffset.UTC;
	
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
        
    @Override
    public void declareConnections(Hardware c) {
    	c.connectDigital(Button, BUTTON_CONNECTION);
    	c.useI2C();
    	c.setTriggerRate(50);
    }

    //TODO: rewrite this a a class, can not be done as two lambddas and be responsvie.

    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	
    	CommandChannel channel = runtime.newCommandChannel();
    	
    	runtime.addDigitalListener((connection, time, durationMillis, value)->{
    		    		
    		if (0 == value) {    			
    			//how long was it pressed before this change to up?
    			if (durationMillis>1000) {    				
    				startTime = 0;
    				stopTime = 0;
    				running = false;
    			} else {
    				running = startOnUp;
    				startOnUp = false;
    			}
    		} else {
    			//user button down
    			//toggle clock start or stop
    			if (!running) {
    				if (0==startTime) {
    					startTime = System.currentTimeMillis();
    				} else {
    					startTime = System.currentTimeMillis()-(stopTime-startTime);
    					stopTime = 0;
    				}
    				startOnUp = true;
    			} else {
    				stopTime = System.currentTimeMillis();
    				running = false;
    			}
    			
    		}
    		
    	});
    	
    	final CommandChannel lcdTextChannel = runtime.newCommandChannel();
    	runtime.addTimeListener((time)->{ 
    		
    		    
    		
    		  long duration = 0==startTime? 0 :  stopTime==0 ? time-startTime :  stopTime-startTime;
    		
    		  LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(duration), zone);
    		
    		  String text = date.format(formatter);

			  Grove_LCD_RGB.commandForText(lcdTextChannel, text);
    		
    	});
    	
    }
        
  
}
