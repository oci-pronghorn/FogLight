package com.coiweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.ThreeAxisAccelerometer_16g;

public class DeclareConnections implements FogApp
{
    
	private static final Port BUZZER_PORT = D2;	//Declaring these variables allows eaiser use in the main body of your code
	private static final Port BUTTON_PORT = D3;
	


    @Override
    public void declareConnections(Hardware c) {

    	//Declaring connections for analog or digital devices
    	c.connect(Buzzer, BUZZER_PORT);
    	c.connect(Button, BUTTON_PORT, 1, 1, true); //Optional: customRateMS - set the rate at which the device updates
    												//customAvgWinMS - set the rate at which the data is sampled
    												//everyValue - can cause the device to trigger events on every update
    	
    	//Declaring connections for I2C devices
    	c.useI2C();
    	c.connect(OLED_96x96, 1); //Optional: customRateMS - set the rate at which the device updates
    	
    	//Declaring connections for serial devices
    	c.useSerial(Baud.B__1000000, "GPS"); //Optional: the device can be set as a second argument
    	c.limitThreads(); //picks the optimal threads based on core detection, however, you can also add an int argument 
    					  //to assign a specific amount of threads
    	
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {

    }
          
}
