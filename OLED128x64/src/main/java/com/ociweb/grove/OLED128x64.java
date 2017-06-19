package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.Grove_FourDigitDisplay;
import com.ociweb.iot.grove.Grove_OLED_128x64;

public class OLED128x64 implements FogApp
{
    


    @Override
    public void declareConnections(Hardware c) {
        c.useI2C();
        c.setTriggerRate(500);
        //TODO: give warning message if trigger rate was not set and time listener is used
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	final FogCommandChannel ch = runtime.newCommandChannel();
    	runtime.addStartupListener(()->{	
			System.out.println("Started");
			Grove_OLED_128x64.init(ch);
		});
    	runtime.addTimeListener((time,iteration) ->{
    		int brightness = (int)(Math.sin(time/(1000.0*Math.PI)) * 255 + 255);
    		System.out.println(brightness);	
    		Grove_OLED_128x64.setContrast(ch, brightness);
    	});
    	
    }
          
}
