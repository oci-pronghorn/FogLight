/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.RTC.*;
import com.ociweb.iot.grove.RealTimeClock.RTC_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class RTCBehavior implements I2CListener, StartupListener {
    private final FogCommandChannel ch;
    private final RTC_Facade clock;
    
    public RTCBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        clock = RTC.newFacade(ch);
    }
    
    
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        int[] temp = clock.intepretData(backing, position, length, mask);
        clock.printTime(temp);
    }
    
    @Override
    public void startup() {
        //            clock.startClock();
        //            clock.setTime(0, 50, 13, 3, 28, 6, 17);

    }
    
}
