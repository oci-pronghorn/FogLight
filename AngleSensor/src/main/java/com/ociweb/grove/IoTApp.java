package com.ociweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.*;

public class IoTApp implements FogApp
{
    private static final Port LED1_PORT = D3;
    private static final Port LED2_PORT = D4;
    private static final Port ANGLE_SENSOR = A0;
    
    @Override
    public void declareConnections(Hardware c) {

        c.connect(LED, LED1_PORT);
        c.connect(LED,LED2_PORT);
        c.connect(AngleSensor,ANGLE_SENSOR);
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        final FogCommandChannel led1Channel = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        final FogCommandChannel led2Channel = runtime.newCommandChannel(DYNAMIC_MESSAGING);

        runtime.addAnalogListener((port, time, durationMillis, average, value)->{
            if(value>512){
                led2Channel.setValue(LED2_PORT,true);
            }else{
                led2Channel.setValue(LED2_PORT,false);
            }
            led1Channel.setValue(LED1_PORT,value/4);
        }).includePorts(ANGLE_SENSOR); 

    }
        
  
}
