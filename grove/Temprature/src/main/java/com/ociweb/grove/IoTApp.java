package com.ociweb.grove;


import static com.ociweb.iot.maker.Port.A0;

import com.ociweb.iot.grove.temp_and_humid.TempAndHumidTwig;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        //c.connect(new TempAndHumidTwig(A0.port,TempAndHumidTwig.MODULE_TYPE.DHT2301));
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new TempSensorBehavior(runtime));

    }  
}
