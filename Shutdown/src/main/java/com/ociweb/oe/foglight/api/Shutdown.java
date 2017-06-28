package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Shutdown implements FogApp
{	
	private static final Port LED_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private static boolean statusOfLED = false;
	
	/*public static void main(String[] args){
		FogRuntime.run(new Shutdown());
	}
	*/
	
    @Override
    public void declareConnections(Hardware c) {
        
    	c.connect(LED, D2);
    	c.connect(Button, D3);
    	c.enableTelemetry(false);
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        
    	final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	final FogCommandChannel channel2 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	final FogCommandChannel channel3 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	final FogCommandChannel channel4 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
    	
    	runtime.addStartupListener(()->{
    		channel1.setValue(LED_PORT, true);
    		statusOfLED = true;
    		System.out.println("The Light is on");
    	});   	

    	
    	runtime.addDigitalListener((port, connection, time, value)-> {
    		if(value == 1){
    			System.out.println("Starting the shutdown process");
    			channel2.block(100);
    			channel2.publishTopic("Shutdown", writable->{});
    		}
    	});    	
    	
    	
    	runtime.addPubSubListener((topic, payload)->{
			if(statusOfLED){
				System.out.println("The light is still on, it must be turned off before shutdown");
				channel3.block(100);
				channel3.publishTopic("LED", writable->{});
			}
			else{
				System.out.println("Shutting down now");
				runtime.shutdownRuntime();
			}
			return true;
    	}).addSubscription("Shutdown");

    	    	
    	runtime.addPubSubListener((topic, payload)->{
    		System.out.println("Turning off the light");
    		channel4.setValue(LED_PORT, false);
    		statusOfLED = false;
    		channel4.block(100);
    		channel4.publishTopic("Shutdown", writable->{});
    		return true;
    	}).addSubscription("LED");
    }
          
}
