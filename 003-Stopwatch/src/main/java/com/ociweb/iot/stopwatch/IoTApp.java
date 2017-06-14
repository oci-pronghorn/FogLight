package com.ociweb.iot.stopwatch;


import static com.ociweb.iot.grove.GroveTwig.Button;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
	public static final Port BUTTON_CONNECTION = D3; //long press clear, short press start/top
	
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	
	private boolean running;
	private boolean startOnUp;
	private long startTime;
	private long stopTime;
	
	private final ZoneId zone = ZoneOffset.UTC;
	
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
        
    @Override
    public void declareConnections(Hardware c) {
    	c.connect(Button, BUTTON_CONNECTION);
    	c.useI2C();
    	c.setTriggerRate(50);
    }

    //TODO: rewrite this a a class, can not be done as two lambddas and be responsvie.

    @Override
    public void declareBehavior(FogRuntime runtime) {
    	
    	FogCommandChannel channel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	
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
    	
    	final FogCommandChannel lcdTextChannel = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addTimeListener((time, instance)->{ 
    		
    		    
    		
    		  long duration = 0==startTime? 0 :  stopTime==0 ? time-startTime :  stopTime-startTime;
    		
    		  LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(duration), zone);
    		
    		  String text = date.format(formatter);

			  Grove_LCD_RGB.commandForText(lcdTextChannel, text);
    		
    	});
    	
    }
        
  
}
