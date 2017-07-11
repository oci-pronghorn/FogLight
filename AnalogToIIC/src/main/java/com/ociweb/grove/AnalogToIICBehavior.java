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
        sensor.setCONFIG_REG(0x28);
        sensor.setHysteresis(100);
        sensor.setUpperLimit(2000);
    }
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        if(register == ADC_Constants.REG_ADDR_RESULT){
            short value = sensor.interpretData(backing, position, length, mask);
            System.out.println("value: "+value);
            
            int alert = sensor.readAlertFlag(backing, position, length, mask);
            System.out.println("alert: "+alert);
        }
        if(register == ADC_Constants.REG_ADDR_ALERT){
            System.out.println("upper/lower: "+(backing[position]&0x03));
        }
        
    }

    
    
}
