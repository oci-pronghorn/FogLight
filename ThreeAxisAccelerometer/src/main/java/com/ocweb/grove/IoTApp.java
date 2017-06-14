package com.ocweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements IoTSetup
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
    // // by using constants such as these you can easily use the right value to reference where the sensor was plugged in
      
    //private static final Port BUTTON_PORT = D3;
	//private static final Port LED_PORT    = D4;
    //private static final Port RELAY_PORT  = D7;
    //private static final Port LIGHT_SENSOR_PORT= A2;

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        //c.connect(Button, BUTTON_PORT); 
        //c.connect(Relay, RELAY_PORT);         
        //c.connect(LightSensor, LIGHT_SENSOR_PORT); 
        //c.connect(LED, LED_PORT);        
        //c.useI2C();
        
    }


    @Override
    public void declareBehavior(DeviceRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        //  //Use lambdas or classes and add listeners to the runtime object
        //  //CommandChannels are created to send outgoing events to the hardware
        //  //CommandChannels must never be shared between two lambdas or classes.
        //  //A single lambda or class can use mulitiple CommandChannels for cuoncurrent behavior
        
        
        //        final CommandChannel channel1 = runtime.newCommandChannel();
        //        //this digital listener will get all the button press and un-press events 
        //        runtime.addDigitalListener((connection, time, value)->{ 
        //            
        //            //connection could be checked but unnecessary since we only have 1 digital source
        //            
        //            if (channel1.digitalSetValue(RELAY_PORT, value)) {
        //                //keep the relay on or off for 1 second before doing next command
        //                channel1.digitalBlock(RELAY_PORT, 1000); 
        //            }
        //        });
    }
        
  
}
