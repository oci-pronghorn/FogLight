package com.ociweb.oe.foglight.api;

import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D3;

import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.ShutdownListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.pipe.BlobReader;

public class ShutdownBehavior implements StartupListener, DigitalListener, ShutdownListener, PubSubListener{

	private static final Port LED_PORT = D2;
	private static final Port BUTTON_PORT = D3;
	private boolean SdRequested = false; // shutdown requested
	private boolean PSRequested = false; // pubsub requested
	private boolean statusOfLED = false; // LED on or off
	private boolean SdConfirmed = false; //shutdown confirmed
	
	final FogCommandChannel channel1;

	private final FogRuntime runtime;

   
	
    public ShutdownBehavior(FogRuntime runtime) {
		channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING | FogCommandChannel.I2C_WRITER | FogCommandChannel.PIN_WRITER);
		this.runtime = runtime;
	}
	
	@Override
	public void startup() {
		channel1.setValue(LED_PORT, true);
		statusOfLED = true;
		System.out.println("The Light is on");
	}
	
	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {
		if(value == 1 && !SdRequested){
			System.out.println("Starting the shutdown process");
			SdRequested = true;
		
			runtime.shutdownRuntime();
    		
		}
	}
	
	@Override
	public boolean acceptShutdown() {
		if(statusOfLED){
			if(!PSRequested){
				PSRequested = true;
				System.out.println("Checking and turning off light");
    			    			
				channel1.setValue(LED_PORT, false);   			
				
    			channel1.publishTopic("LED");
			}
			return false;
		}
		else if(!SdConfirmed){
			SdConfirmed = true;
			System.out.println("Shutting down");
			
			return true;
		}
		return false;
	}
	
	

	@Override
	public boolean message(CharSequence topic, BlobReader payload) {
		statusOfLED = false;
		return true;
	}
	
	

	
}
