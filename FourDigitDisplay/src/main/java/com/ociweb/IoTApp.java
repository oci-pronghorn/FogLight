package com.ociweb;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.four_digit_display.FourDigitDisplay;
import com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay;

public class IoTApp implements FogApp
{
	@Override
	public void declareConnections(Hardware c) {
		c.setTriggerRate(1);
	}
	
	public static void main(String[] args){
		FogRuntime.run(new IoTApp());
	}

	@Override
	public void declareBehavior(FogRuntime runtime) {
		final FogCommandChannel ch = runtime.newCommandChannel();
		FourDigitDisplay display = Grove_FourDigitDisplay.newObj(ch, D5);
		runtime.addStartupListener(()->{
			System.out.println("Port byte: " + D5.port);	
			display.init();
			display.setBrightness(7);
			display.displayOn();
		});
		runtime.addTimeListener((time, iteration)->{
			display.printFourDigitsWithColon((iteration/100)%100, iteration % 100);
		});
		
	}
}
