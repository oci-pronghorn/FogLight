package com.ociweb.iot.trafficlight;


import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.PayloadWriter;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;
public class IoTApp implements IoTSetup
{
	private static final Port LED3_PORT = D5;
	private static final Port LED1_PORT = D7;
	private static final Port LED2_PORT = D8;	
	
	private enum State {
		REDLIGHT(10000), GREENLIGHT(8000),YELLOWLIGHT(2000);
		private int deltaTime;
		State(int deltaTime){this.deltaTime=deltaTime;}
		public int getTime(){return deltaTime;}
	};
	
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
		c.connect(LED, LED1_PORT);
		c.connect(LED, LED2_PORT);
		c.connect(LED, LED3_PORT);
		c.useI2C();
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
    	
    	final CommandChannel channel0 = runtime.newCommandChannel();
    	runtime.addPubSubListener((topic, payload)-> {
    		
    		channel0.setValueAndBlock(LED1_PORT, 1, State.REDLIGHT.getTime());
			channel0.setValueAndBlock(LED2_PORT, 0, State.REDLIGHT.getTime());
			channel0.setValueAndBlock(LED3_PORT, 0, State.REDLIGHT.getTime());
			Grove_LCD_RGB.commandForTextAndColor(channel0, "RED", 255, 0, 0);
						
			channel0.openTopic("GREEN").publish();
			
    	}).addSubscription("RED");

    	final CommandChannel channel1 = runtime.newCommandChannel();
    	runtime.addPubSubListener((topic, payload)-> {
    		channel1.setValueAndBlock(LED1_PORT, 0, State.GREENLIGHT.getTime());
			channel1.setValueAndBlock(LED2_PORT, 0, State.GREENLIGHT.getTime());
			channel1.setValueAndBlock(LED3_PORT, 1, State.GREENLIGHT.getTime());
			Grove_LCD_RGB.commandForTextAndColor(channel1, "GREEN",0, 255, 0);
			channel1.openTopic("YELLOW").publish();
    		
    	}).addSubscription("GREEN");

    	final CommandChannel channel2 = runtime.newCommandChannel();
    	runtime.addPubSubListener((topic, payload)-> {
    		channel2.setValueAndBlock(LED1_PORT, 0,State.YELLOWLIGHT.getTime());
			channel2.setValueAndBlock(LED2_PORT, 1,State.YELLOWLIGHT.getTime());
			channel2.setValueAndBlock(LED3_PORT, 0,State.YELLOWLIGHT.getTime());
			Grove_LCD_RGB.commandForTextAndColor(channel2,"YELLOW", 255, 255, 0);
			channel2.openTopic("RED").publish();
    		
    	}).addSubscription("YELLOW");
    	
       final CommandChannel channel4 = runtime.newCommandChannel();
       runtime.addStartupListener(()->{channel4.openTopic("RED").publish();});
        
    }
        
  
}
