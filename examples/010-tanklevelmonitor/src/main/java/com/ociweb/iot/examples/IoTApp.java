package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.UltrasonicRanger;
import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.Button;
import static com.ociweb.iot.maker.Port.A0;
import static com.ociweb.iot.maker.Port.D2;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{

    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        c.connect(Button, D2);
        c.connect(UltrasonicRanger, A0);
        
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
    	 runtime.registerListener(new IoTBehavior(runtime));
    }
        
  
}
