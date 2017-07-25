/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;


import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.real_time_clock.RTCListener;
import com.ociweb.iot.grove.real_time_clock.RTC_Transducer;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.maker.FogRuntime.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class ClockBehavior implements RTCListener,Behavior{

    private FogCommandChannel ch;
    RTC_Transducer clock;
    public ClockBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        clock = new RTC_Transducer(ch,this);
    }

    @Override
    public void clockVals(int[] vals) {
        clock.printTime(vals);

    }

}
