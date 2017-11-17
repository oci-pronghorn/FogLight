package com.ociweb.oe.foglight.api;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

import static com.ociweb.iot.maker.Port.D2;
import static com.ociweb.iot.maker.Port.D3;

public class CmdChannelBehavior implements DigitalListener {
	
private static final Port LED_PORT = D2;
private static final Port BUTTON_PORT = D3;

	
	private final FogCommandChannel channel1;

	public CmdChannelBehavior(FogRuntime runtime) {
		channel1 = runtime.newCommandChannel( FogCommandChannel.PIN_WRITER | DYNAMIC_MESSAGING);
	}

	@Override
	public void digitalEvent(Port port, long time, long durationMillis, int value) {

		//channel1.setValueAndBlock(LED_PORT, value == 1, 500); //This method could be used on its own
		
		channel1.setValue(LED_PORT, value == 1); 
		
		channel1.block(500); //This block method will stop anything from going through this channel for the specified amount of milliseconds
		
		//channel1.blockUntil(1514764800000); //this will block until the specified epoch time, 
		
		//channel1.block(BUTTON_PORT, 500); //This block method only stop any commands for the specified port, but other uses of the command channel will still be active
		channel1.shutdown();

	}

}
