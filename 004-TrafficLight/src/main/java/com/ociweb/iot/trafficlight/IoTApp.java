package com.ociweb.iot.trafficlight;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.hardware.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
	private static final int LED3_CONNECTION = 3;
	private static final int LED_CONNECTION = 5;
	private static final int LED2_CONNECTION = 6;
	private static long prevTime =0;
	private enum State {
		REDLIGHT(1200), GREENLIGHT(1000),YELLOWLIGHT(200);
		private int deltaTime;
		State(int deltaTime){this.deltaTime=deltaTime;}
		public int getTime(){return deltaTime;}
	};
	private State state = State.YELLOWLIGHT;
       
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
		c.connectDigital(LED, LED_CONNECTION);
		c.connectDigital(LED, LED2_CONNECTION);
		c.connectDigital(LED, LED3_CONNECTION);
		c.setTriggerRate(500);
		c.useI2C();
        
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
		final CommandChannel channel1 = runtime.newCommandChannel();
		final CommandChannel channellcd=runtime.newCommandChannel();




		runtime.addTimeListener((time)->{ 

			switch(state){
			case YELLOWLIGHT:
				channel1.digitalSetValue(6, 0);
				channel1.digitalSetValue(5, 1);
				channel1.digitalSetValue(3, 0);
				Grove_LCD_RGB.commandForTextAndColor(channellcd,"YELLOW", 255, 255, 0);
				if(time-prevTime>=state.getTime()){
					state = State.REDLIGHT;
					prevTime = time;
				}
				break;
			case REDLIGHT:
				channel1.digitalSetValue(6, 1);
				channel1.digitalSetValue(5, 0);
				channel1.digitalSetValue(3, 0);
				Grove_LCD_RGB.commandForTextAndColor(channellcd, "RED", 255, 0, 0);
				if(time-prevTime>=state.getTime()){
					state = State.GREENLIGHT;
					prevTime = time;
				}
				break;
			case GREENLIGHT:
				channel1.digitalSetValue(6, 0);
				channel1.digitalSetValue(5, 0);
				channel1.digitalSetValue(3, 1);
				Grove_LCD_RGB.commandForTextAndColor(channellcd, "GREEN",0, 255, 0);
				if(time-prevTime>=state.getTime()){
					state = State.YELLOWLIGHT;
					prevTime = time;
				}
				break;
			}
		});
    }
        
  
}
