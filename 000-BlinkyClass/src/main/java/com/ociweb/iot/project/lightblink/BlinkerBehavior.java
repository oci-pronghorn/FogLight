/**
 * blinkerChannel is a CommandChannel created to transport data. 
 * Data is published to the channel, and it can also be accessed 
 * by playload.writeInt() to read from the channel
 * <p>
 * The blinky light is achieved by alternate the value publish to
 * the channel. The method shown below enable to turn on/off the 
 * LED as well as block the command channel for the duration of a 
 * blink
 * digitalSetValueAndBlock(int connector, int value, long msDuration)
 * @param  connector  the LED connect to the digital connector D5
 * @param  value      1 is turn on the LED while 0 turn off the LED
 * @param  msDuration how long the channel is blocked till it is available for next command
 * @return      return true when the method operated successfully 
 */


package com.ociweb.iot.project.lightblink;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;

public class BlinkerBehavior implements StartupListener, PubSubListener {
 
    private static final String TOPIC = "light";
    private static final int PAUSE = 500;
	
	private FogCommandChannel blinkerChannel;
	
	public BlinkerBehavior(FogRuntime runtime) {
		blinkerChannel = runtime.newCommandChannel( FogRuntime.PIN_WRITER |
			    FogApp.DYNAMIC_MESSAGING); 
	}	
	
	@Override
	public boolean message(CharSequence topic, MessageReader payload) {

		 int value = payload.readInt();
         blinkerChannel.setValueAndBlock(IoTApp.LED_PORT, value==1, PAUSE);               
         return blinkerChannel.publishTopic(TOPIC, w->{
        	 w.writeInt( 1==value ? 0 : 1 );        	 
         });
    }

	@Override
	public void startup() {

		blinkerChannel.subscribe(TOPIC, this);
		blinkerChannel.publishTopic(TOPIC,w->{
			w.writeInt( 1 );
		});
	}

}
