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
import static com.ociweb.grove.IoTApp.*;


/**
 *
 * @author huydo
 */
public class LineFinderBehavior implements DigitalListener {
        
    private final FogCommandChannel ledChannel;
    
    public LineFinderBehavior(FogRuntime runtime){
        this.ledChannel = runtime.newCommandChannel(PIN_WRITER);
    }
    @Override
    public void digitalEvent(Port port, long time, long durationMillis, int value) {
        ledChannel.setValue(LED_PORT,value==1);
        if(value == 1){
            System.out.println("Door just close. Time the door remained opened: "+durationMillis);
        }else{
            System.out.println("Door just open. Time the door remained closed: "+durationMillis);
        }
    }
    
}
