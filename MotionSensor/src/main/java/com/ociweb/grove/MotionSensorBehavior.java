/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;
import static com.ociweb.iot.maker.Port.D4;

/**
 *
 * @author huydo
 */
public class MotionSensorBehavior implements DigitalListener{
    private static final Port LED_PORT = D4;
    
    private final FogCommandChannel ledChannel;
    
    public MotionSensorBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel();
    }
    
    @Override
    public void digitalEvent(Port port, long time, long durationMillis, int value) {
        ledChannel.setValue(LED_PORT,value==1);
        System.out.println("Stop moving!");
    }
    
}
