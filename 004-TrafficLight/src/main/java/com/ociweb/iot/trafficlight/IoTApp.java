package com.ociweb.iot.trafficlight;


import static com.ociweb.iot.grove.GroveTwig.LED;
import static com.ociweb.iot.maker.Port.D3;
import static com.ociweb.iot.maker.Port.D5;
import static com.ociweb.iot.maker.Port.D6;

import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
public class IoTApp implements IoTSetup
{
	private static final Port LED3_PORT = D5;
	private static final Port LED1_PORT = D3;
	private static final Port LED2_PORT = D6;	
	
	public static int RED_MS = 10000;
	public static int GREEN_MS = 8000;
	public static int YELLOW_MS = 2000;
			
	private boolean isWebControlled = false;////set this to true;
	
	private int webRoute = -1;
	private byte[] COLOR = "color".getBytes();
			
	private byte[] RED = "red".getBytes();
	private byte[] GREEN = "green".getBytes();
	private byte[] YELLOW = "yellow".getBytes();
		
	
	private enum State {
		REDLIGHT(RED_MS), 
		GREENLIGHT(GREEN_MS),
		YELLOWLIGHT(YELLOW_MS);
		private int deltaTime;
		State(int deltaTime){this.deltaTime=deltaTime;}
		public int getTime(){return deltaTime;}
	}
	
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
		c.connect(LED, LED1_PORT);
		c.connect(LED, LED2_PORT);
		c.connect(LED, LED3_PORT);
		c.useI2C();
		
		//still testing with different sizes...
		c.enableServer(false, true, "127.0.0.1", 8088);
		
		
		if (isWebControlled) {
			
			c.enableTelemetry(true);			
			webRoute = c.registerRoute("/trafficLight?color=${color}");
			
		}
		
		
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	
    	if (isWebControlled) {
    		configureWebBasedColorChange(runtime); 
    	} else {
    		configureTimeBasedColorChange(runtime);
    	}
       
       
    }


	private void configureWebBasedColorChange(DeviceRuntime runtime) {
		final CommandChannel channel = runtime.newCommandChannel(
				GreenCommandChannel.DYNAMIC_MESSAGING | 
				GreenCommandChannel.NET_RESPONDER);


		runtime.addRestListener((reader)->{
									 if (reader.isEqual(COLOR, RED)) {
										 return channel.publishHTTPResponse(reader, turnOnRed(channel) ? 200 : 500);
										 
									 } else if (reader.isEqual(COLOR, GREEN)) {
										 return channel.publishHTTPResponse(reader, turnOnGreen(channel) ? 200 : 500);
										 
									 } else if (reader.isEqual(COLOR, YELLOW)) {
										 return channel.publishHTTPResponse(reader, turnOnYellow(channel) ? 200 : 500);
										 
									 } else {
										 
										 return channel.publishHTTPResponse(reader, 404);
										 
									 }}	, webRoute);
	}


	protected void configureTimeBasedColorChange(DeviceRuntime runtime) {
		final CommandChannel channel0 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addPubSubListener((topic, payload)-> {
    		
    		turnOnRed(channel0);
			channel0.block(State.REDLIGHT.getTime());
			
			channel0.openTopic("GREEN").ifPresent(w->{w.publish();});
			return true;
    	}).addSubscription("RED");

    	final CommandChannel channel1 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addPubSubListener((topic, payload)-> {
    		
    		turnOnGreen(channel1);
			channel1.block(State.GREENLIGHT.getTime());
			
			channel1.openTopic("YELLOW").ifPresent(w->{w.publish();});
			return true;
    	}).addSubscription("GREEN");

    	final CommandChannel channel2 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
    	runtime.addPubSubListener((topic, payload)-> {
    		
    		turnOnYellow(channel2);
			channel2.block(State.YELLOWLIGHT.getTime());
			
			channel2.openTopic("RED").ifPresent(w->{w.publish();});
			return true;
    	}).addSubscription("YELLOW");
    	
       final CommandChannel channel4 = runtime.newCommandChannel(GreenCommandChannel.DYNAMIC_MESSAGING);
       runtime.addStartupListener(()->{channel4.openTopic("RED").ifPresent(w->{w.publish();});});
	}


	private boolean turnOnGreen(final CommandChannel c) {
		return 
		c.setValue(LED1_PORT, 0) |
		c.setValue(LED2_PORT, 0) |
		c.setValue(LED3_PORT, 1) |
		Grove_LCD_RGB.commandForTextAndColor(c, "GREEN",0, 255, 0);
	}


	private boolean turnOnYellow(final CommandChannel c) {
		return
		c.setValue(LED1_PORT, 0) |
		c.setValue(LED2_PORT, 1) |
		c.setValue(LED3_PORT, 0) |
		Grove_LCD_RGB.commandForTextAndColor(c,"YELLOW", 255, 255, 0);
	}


	private boolean turnOnRed(final CommandChannel c) {
		return
		c.setValue(LED1_PORT, 1) |
		c.setValue(LED2_PORT, 0) |
		c.setValue(LED3_PORT, 0) |
		Grove_LCD_RGB.commandForTextAndColor(c, "RED", 255, 0, 0);
	}

     
  
}
