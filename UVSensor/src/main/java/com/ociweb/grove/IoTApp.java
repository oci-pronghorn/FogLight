package com.ociweb.grove;


import static com.ociweb.iot.grove.AnalogDigitalGroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{

    private static final Port UV_SENSOR_PORT = A2;

    @Override
    public void declareConnections(Hardware c) {

        c.connect(UVSensor, UV_SENSOR_PORT,500);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
      
        runtime.addAnalogListener(new UVSensorBehavior(runtime));
    }
}
