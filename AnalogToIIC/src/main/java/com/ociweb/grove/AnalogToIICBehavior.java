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
import static com.ociweb.iot.maker.FogRuntime.*;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class AnalogToIICBehavior implements StartupListener,AlertStatusListener,ConversionResultListener{
    private final FogCommandChannel ch;
    private final ADC_Transducer sensor;
    private int check;
    private final int upperLimit = 1100;
    public AnalogToIICBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        sensor = new ADC_Transducer(ch,this);
    }
    @Override
    public void startup() {
        sensor.begin();
        sensor.setCONFIG_REG(0b00111000);
        //sensor.setHysteresis(100);
        sensor.setUpperLimit(upperLimit);
//        int[] start = {0,0};
//        sensor.writeTwoBytesToRegister(ADC_Constants.REG_ADDR_CONVH,start);
    }


    @Override
    public void conversionResult(int result) {
        if(check == 1){
            if(result < upperLimit){
                System.out.println("if you're happy and you know it,");
                sensor.setCONFIG_REG(0b00101000);
                sensor.setCONFIG_REG(0b00111000);
            }
        }
    }

    @Override
    public void alertStatus(int status) {
        check = status;
        
    }
    
}
