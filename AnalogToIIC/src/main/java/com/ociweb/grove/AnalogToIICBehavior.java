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

/**
 *
 * @author huydo
 */
public class AnalogToIICBehavior implements StartupListener,ADCListener{
    private final FogCommandChannel ch;
    private final ADC_Facade sensor;
    
    public AnalogToIICBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        sensor = new ADC_Facade(ch,this);
        runtime.registerListener(sensor);
    }
    @Override
    public void startup() {
        sensor.begin();
        sensor.setCONFIG_REG(0x28);
        sensor.setHysteresis(100);
        sensor.setUpperLimit(2000);
    }


    @Override
    public void conversionResult(int result) {
        System.out.println(result);
    }

    @Override
    public void alertStatus(int status) {
        System.out.println(status);
    }

    
    
}
