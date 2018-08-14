/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import static com.ociweb.grove.IoTApp.LED1_PORT;
import static com.ociweb.grove.IoTApp.LED2_PORT;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

/**
 *
 * @author huydo
 */
public class AngleSensorBehavior implements AnalogListener {

    private final FogCommandChannel led1Channel;
    private    final FogCommandChannel led2Channel;
    
    public AngleSensorBehavior(FogRuntime runtime){
        this.led1Channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER);
        this.led2Channel = runtime.newCommandChannel(FogCommandChannel.PIN_WRITER);
    }
        
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        if(value>512){
                led2Channel.setValue(LED2_PORT,true);
            }else{
                led2Channel.setValue(LED2_PORT,false);
            }
            led1Channel.setValue(LED1_PORT,value/4);
    }
    
}
