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
public class AnalogToIICBehavior implements StartupListener,AlertStatusListener,ConversionResultListener{
    private final FogCommandChannel ch;
    private final ADC_Transducer sensor;
    private final int upperLimit = 400;
    public AnalogToIICBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        sensor = new ADC_Transducer(ch,this);
    }
    @Override
    public void startup() {
        sensor.setAlertHoldBit(true);
        sensor.setAlertFlagEnableBit(true);
        sensor.setUpperLimit(upperLimit);
    }


    @Override
    public void conversionResult(int result) {

    }

    @Override
    public void alertStatus(int overRange, int underRange) {
        if(overRange == 1){
            System.out.println("clap detected");
            sensor.setAlertHoldBit(false);
            sensor.setAlertHoldBit(true);
        }
    }

}
