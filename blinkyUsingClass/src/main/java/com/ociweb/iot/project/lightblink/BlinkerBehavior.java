package com.ociweb.iot.project.lightblink;

import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.IOTDeviceRuntime;
import com.ociweb.iot.maker.PayloadReader;
import com.ociweb.iot.maker.PubSubListener;
import com.ociweb.iot.maker.StartupListener;

public class BlinkerBehavior implements StartupListener, PubSubListener {
 
    private static final String TOPIC = "light";
    private static final int PAUSE = 500;
	
	private CommandChannel blinkerChannel;
	
	public BlinkerBehavior(IOTDeviceRuntime runtime) {
		blinkerChannel = runtime.newCommandChannel(); 
	}	
	
	@Override
	public void message(CharSequence topic, PayloadReader payload) {
		 int value = payload.readInt();
         blinkerChannel.digitalSetValueAndBlock(IoTApp.LED_CONNECTION, value, PAUSE);               
         blinkerChannel.openTopic(TOPIC).writeInt( 1==value ? 0 : 1 ).publish();
    }

	@Override
	public void startup() {
		blinkerChannel.subscribe(TOPIC, this);
		blinkerChannel.openTopic(TOPIC).writeInt( 1 ).publish();   
	}

}
