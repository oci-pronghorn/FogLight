package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.Button;
import static com.ociweb.iot.grove.GroveTwig.LED;
import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D3;

import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

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
    public void declareBehavior(final FogRuntime runtime) {
        
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
    			//1
    			runtime.shutdownRuntime();
    			
    			//2
    			ReactiveListenerStage.requestSystemShutdown(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
					}
    			});
    			
    			//3
    			ReactiveListenerStage.requestSystemShutdown(shutdownRunnable);
    			
    			//4
    			runtime.requestSystemShutdown(shutdownRunnable);
    		}
    	});    	
    	
    	
    	runtime.addShutdownListener(()->{
    		//check if light is on, if it is, return false and turn it off, if not, return true
    		return true;
    	});
    	
    	runtime.addPubSubListener((topic, payload)->{
    		return true;
    	});
    	
    }
          
}
