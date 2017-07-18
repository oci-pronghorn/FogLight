package com.ociweb.grove;

import com.ociweb.iot.maker.AnalogListener;
import static com.ociweb.iot.maker.FogCommandChannel.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

public class UVSensorBehavior implements AnalogListener {
    public UVSensorBehavior(FogRuntime runtime) {   
    }
    
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        // TODO Auto-generated method stub
        System.out.println("The Illumination intensity is : "+(value/1023*307)+"mW/m^2");
        
    }
    
}
