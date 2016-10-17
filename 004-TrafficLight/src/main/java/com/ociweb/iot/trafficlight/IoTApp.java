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
	private static final Port LED1_PORT = D3;
	private static final Port LED2_PORT = D6;	
	
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
    		
    		channel0.setValue(LED1_PORT, 1);
			channel0.setValue(LED2_PORT, 0);
			channel0.setValue(LED3_PORT, 0);
			Grove_LCD_RGB.commandForTextAndColor(channel0, "RED", 255, 0, 0);		
			
			//Not the way this should be written...
			//NOTE: this is a hack until the block becomes common between native I2C and grovePi translated pins
			try {
				Thread.sleep(State.REDLIGHT.getTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TODO: switch to using this
			//channel0.block(State.REDLIGHT.getTime());
			
			
			channel0.openTopic("GREEN").publish();
			
    	}).addSubscription("RED");

    	final CommandChannel channel1 = runtime.newCommandChannel();
    	runtime.addPubSubListener((topic, payload)-> {
    		channel1.setValue(LED1_PORT, 0);
			channel1.setValue(LED2_PORT, 0);
			channel1.setValue(LED3_PORT, 1);
			Grove_LCD_RGB.commandForTextAndColor(channel1, "GREEN",0, 255, 0);
			
			
			//Not the way this should be written...
			//NOTE: this is a hack until the block becomes common between native I2C and grovePi translated pins
			try {
				Thread.sleep(State.GREENLIGHT.getTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TODO: switch to using this
			//channel1.block(State.GREENLIGHT.getTime());
			
			channel1.openTopic("YELLOW").publish();
    		
    	}).addSubscription("GREEN");

    	final CommandChannel channel2 = runtime.newCommandChannel();
    	runtime.addPubSubListener((topic, payload)-> {
    		channel2.setValue(LED1_PORT, 0);
			channel2.setValue(LED2_PORT, 1);
			channel2.setValue(LED3_PORT, 0);
			Grove_LCD_RGB.commandForTextAndColor(channel2,"YELLOW", 255, 255, 0);
			
			//Not the way this should be written...
			//NOTE: this is a hack until the block becomes common between native I2C and grovePi translated pins
			try {
				Thread.sleep(State.YELLOWLIGHT.getTime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TODO: switch to using this
			//channel2.block(State.YELLOWLIGHT.getTime());
			
			channel2.openTopic("RED").publish();
    		
    	}).addSubscription("YELLOW");
    	
       final CommandChannel channel4 = runtime.newCommandChannel();
       runtime.addStartupListener(()->{channel4.openTopic("RED").publish();});
        
    }
        
  
}
