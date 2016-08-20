package com.ociweb.iot.trafficlight;


import static com.ociweb.iot.grove.GroveTwig.LED;

import com.ociweb.iot.grove.Grove_LCD_RGB;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.IoTSetup;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;
public class IoTApp implements IoTSetup
{
	private static final Port LED3_PORT = D3;
	private static final Port LED1_PORT = D7;
	private static final Port LED2_PORT = D8;
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
		c.connect(LED, LED1_PORT);
		c.connect(LED, LED2_PORT);
		c.connect(LED, LED3_PORT);
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
				channel1.setValue(LED1_PORT, 0);
				channel1.setValue(LED2_PORT, 1);
				channel1.setValue(LED3_PORT, 0);
				Grove_LCD_RGB.commandForTextAndColor(channellcd,"YELLOW", 255, 255, 0);
				if(time-prevTime>=state.getTime()){
					state = State.REDLIGHT;
					prevTime = time;
				}
				break;
			case REDLIGHT:
				channel1.setValue(LED1_PORT, 1);
				channel1.setValue(LED2_PORT, 0);
				channel1.setValue(LED3_PORT, 0);
				Grove_LCD_RGB.commandForTextAndColor(channellcd, "RED", 255, 0, 0);
				if(time-prevTime>=state.getTime()){
					state = State.GREENLIGHT;
					prevTime = time;
				}
				break;
			case GREENLIGHT:
				channel1.setValue(LED1_PORT, 0);
				channel1.setValue(LED2_PORT, 0);
				channel1.setValue(LED3_PORT, 1);
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
