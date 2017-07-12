/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import com.ociweb.iot.grove.real_time_clock.RTCListener;
import com.ociweb.iot.grove.real_time_clock.RTC_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.maker.FogCommandChannel.I2C_WRITER;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class ClockBehavior implements RTCListener{

    private FogCommandChannel ch;
    RTC_Facade clock; 
    public ClockBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        clock = new RTC_Facade(ch,this);
    }
    
    @Override
    public void clockVals(int[] vals) {
        clock.printTime(vals);

    }
    
}
