/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.grove.adc.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class AnalogToIICBehavior implements I2CListener,StartupListener{
    private final FogCommandChannel ch;
    private final ADC_Facade sensor;
    
    public AnalogToIICBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        sensor = new ADC_Facade(ch);
    }
    @Override
    public void startup() {
        sensor.begin();
    }
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        short value = sensor.intepretData(backing, position, length, mask);
        
        System.out.println(value);
    }

    
    
}
