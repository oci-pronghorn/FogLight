package ${package};


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DeviceRuntime;
import com.ociweb.iot.maker.IoTSetup;

public class IoTApp implements IoTSetup
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
    // // by using constants such as these you can easily use the right value to reference where the sensor was plugged in
      
    //private static final int BUTTON_CONNECTION = 4;
	//private static final int LED_CONNECTION = 5;
    //private static final int RELAY_CONNECTION = 6;
    //private static final int LIGHT_SENSOR_CONNECTION = 0;
       
    
    public static void main( String[] args ) {
        DeviceRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        //c.connectDigital(Button, BUTTON_CONNECTION); 
        //c.connectDigital(Relay, RELAY_CONNECTION);         
        //c.connectAnalog(LightSensor, LIGHT_SENSOR_CONNECTION); 
        //c.connectAnalog(LED, LED_CONNECTION);        
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
        //            if (channel1.digitalSetValue(RELAY_CONNECTION, value)) {
        //                //keep the relay on or off for 1 second before doing next command
        //                channel1.digitalBlock(RELAY_CONNECTION, 1000); 
        //            }
        //        });
    }
        
  
}
