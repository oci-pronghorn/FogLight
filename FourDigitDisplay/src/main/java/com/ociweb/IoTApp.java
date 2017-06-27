package com.ociweb;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.Grove_FourDigitDisplay;
import com.ociweb.iot.grove.display.FourDigitDisplay;

public class IoTApp implements FogApp
{
	//private static final Port CLOCK = D5;
	//private static final Port DATA = D6;

	@Override
	public void declareConnections(Hardware c) {
		c.connect(Grove_FourDigitDisplay.getInstance(), D5);
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
//			display.printDigitAt(2, 3);
//			display.printDigitAt(0, 1);
//			display.printDigitAt(1, 2);
//			display.printDigitAt(3, 4);
			display.printFourDigitsWithColon(12, 34);
		});
		runtime.addTimeListener((time, iteration)->{
		//	String formatted = String.format("%04d", iteration);
//			System.out.println(formatted);
//			String upperPair = String.format("%02d", iteration/100);
//			String lowerPair = String.format("%02d", iteration % 100);
			display.printFourDigitsWithColon(iteration/100, iteration % 100);
		});
		
	}
}
