/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.astropi.AstroPiLEDMatrix;
import com.ociweb.iot.grove.real_time_clock.RTCListener;
import com.ociweb.iot.grove.real_time_clock.RTC_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.maker.FogRuntime.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
//public class ClockBehavior implements RTCListener{
//
//    private FogCommandChannel ch;
//    RTC_Facade clock;
//    public ClockBehavior(FogRuntime runtime){
//        this.ch = runtime.newCommandChannel(I2C_WRITER);
//        clock = new RTC_Facade(ch,this);
//    }
//
//    @Override
//    public void clockVals(int[] vals) {
//        clock.printTime(vals);
//
//    }
//
//}
public class ClockBehavior implements StartupListener,I2CListener{
    
    private final FogCommandChannel ch;
    private final AstroPiLEDMatrix sth;
    ClockBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER,50000);
        sth = new AstroPiLEDMatrix(ch);
    }
    
    
    
    @Override
    public void startup() {
        sth.clear();
        //sth.test();
        int map[][][] =    {{{0,0,0},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63}},
                            {{63,63,63},{63,63,63},{0,0,0},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63}},
                            {{63,63,63},{63,63,63},{63,63,63},{63,63,63},{0,0,0},{63,63,63},{63,63,63},{63,63,63}},
                            {{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{0,0,0}},
                            {{0,0,0},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{0,0,0},{63,63,63}},
                            {{63,63,63},{63,63,63},{63,63,63},{63,63,63},{0,0,0},{63,63,63},{63,63,63},{63,63,63}},
                            {{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63},{63,63,63}},
                            {{63,63,63},{63,63,63},{0,0,0},{63,63,63},{63,63,63},{63,63,63},{0,0,0},{63,63,63}}};
        int[] vals = sth.bitmapToList(map);
        sth.setPixels(vals);
        
        sth.setPixel(0,0,63,0,0);
        sth.setPixel(2,4,0,0,63);
                            
    }

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        System.out.println("value: "+(backing[position]&0xff));
    }
    
    
}