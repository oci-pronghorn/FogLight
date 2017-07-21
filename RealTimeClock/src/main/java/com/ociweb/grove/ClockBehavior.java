/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.astropi.*;

import com.ociweb.iot.grove.real_time_clock.RTCListener;
import com.ociweb.iot.grove.real_time_clock.RTC_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import static com.ociweb.iot.maker.FogRuntime.*;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;
import java.util.Arrays;

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
    private final AstroPi sth;
    private int blink = 0;
    
    ClockBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER,50000);
        sth = new AstroPi(ch);
        //runtime.registerListener(sth);
    }
        int [] R = {63,0,0};
        int [] G = {0,63,0};
        int [] B = {0,0,63};
        int [] W = {0,0,0};
    
    @Override
    public void startup() {

        sth.clear();
//        for(int i=0;i<8;i++){
//
//                sth.setPixel(0,i,B);
//            
//        }
//        sth.setPixel(0,4,R);
//        sth.setRotation(90);
//        ch.i2cDelay(0x46, 1000000000);
//        sth.setRotation(90);
//        ch.i2cDelay(0x46, 1000000000);
//        sth.setRotation(90);
//        ch.i2cDelay(0x46, 1000000000);
 
//        int bitmap[][][] =  {{G,G,G,G,G,G,G,G},
//                             {G,G,R,G,G,G,G,G},
//                             {G,R,G,G,G,G,G,G},
//                             {R,R,R,R,R,R,R,G},
//                             {G,R,G,G,G,G,G,G},
//                             {G,G,R,G,G,G,G,G},
//                             {G,G,G,G,G,G,G,G},
//                             {G,G,G,G,G,G,G,G}};
//        sth.setPixels(bitmap);
//        ch.i2cDelay(0x46, 1000000000);
//        ch.i2cDelay(0x46, 1000000000);
//        sth.flip_h();
//        ch.i2cDelay(0x46, 1000000000);
//        sth.setRotation(90);
//        ch.i2cDelay(0x46, 1000000000);
//        sth.setRotation(180);
//    int bitmap[][][] = {{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {15, 63, 0}, {15, 63, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}}, {{0, 0, 0}, {0, 0, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {0, 0, 0}, {0, 0, 0}}, {{0, 0, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {0, 0, 0}}, {{15, 63, 0}, {15, 63, 0}, {0, 0, 0}, {15, 63, 0}, {15, 63, 0}, {0, 0, 0}, {15, 63, 0}, {15, 63, 0}}, {{15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}, {15, 63, 0}}, {{0, 0, 0}, {0, 0, 0}, {15, 63, 0}, {0, 0, 0}, {0, 0, 0}, {15, 63, 0}, {0, 0, 0}, {0, 0, 0}}, {{0, 0, 0}, {15, 63, 0}, {0, 0, 0}, {15, 63, 0}, {15, 63, 0}, {0, 0, 0}, {15, 63, 0}, {0, 0, 0}}, {{15, 63, 0}, {0, 0, 0}, {15, 63, 0}, {0, 0, 0}, {0, 0, 0}, {15, 63, 0}, {0, 0, 0}, {15, 63, 0}}};
//    sth.setPixels(bitmap);
    sth.setPixel(curRow,curCol,R);
    }


//
//    @Override
//    public void timeEvent(long l, int i) {
//        if(blink == 1){
//            sth.setPixel(3, 2, R);
//            sth.setPixel(3, 5, R);
//            blink = 0;
//        }else{
//            sth.setPixel(3, 2, B);
//            sth.setPixel(3, 5, B);
//            blink = 1;
//        }
//    }

    int curRow = 0;
    int curCol = 0;
    
//    @Override
//    public void joystickEvent(int up, int down, int left, int right, int push) {
//        System.out.println("here");
//        if(up==1){
//            sth.setPixel(curRow, curCol, W);
//            curRow = curRow +1;
//            sth.setPixel(curRow,curCol,R);
//        }else if(down == 1){
//            sth.setPixel(curRow,curCol,W);
//            curRow = curRow -1;
//            sth.setPixel(curRow,curCol,R);
//        }else if(left == 1){
//            sth.setPixel(curRow,curCol,W);
//            curCol = curCol -1;
//            sth.setPixel(curRow,curCol,R);
//        }else if(right ==1){
//            sth.setPixel(curRow,curCol,W);
//            curRow = curRow +1;
//            sth.setPixel(curRow,curCol,R);
//        }
//        
//    }

    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(addr == AstroPi_Constants.LED_I2C_ADDR){
            if(register == AstroPi_Constants.JOYSTICK_REG_ADDR){
                int down = (backing[position]&0x01);
                int right = (backing[position]&0x02)>>1;
                int up = (backing[position]&0x04)>>2;
                int left = (backing[position]&0x1f)>>4;
                if(up==1){
                    sth.setPixel(curRow, curCol, W);
                    curRow = curRow -1;
                    sth.setPixel(curRow,curCol,R);
                }else if(down == 1){
                    sth.setPixel(curRow,curCol,W);
                    curRow = curRow +1;
                    sth.setPixel(curRow,curCol,R);
                }else if(left == 1){
                    sth.setPixel(curRow,curCol,W);
                    curCol = curCol -1;
                    sth.setPixel(curRow,curCol,R);
                }else if(right ==1){
                    sth.setPixel(curRow,curCol,W);
                    curCol = curCol +1;
                    sth.setPixel(curRow,curCol,R);
                }
            }
            
        }
    }
    
    
}