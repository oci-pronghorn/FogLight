/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.astropi.AstroPiLEDMatrix;
import static com.ociweb.iot.maker.FogApp.I2C_WRITER;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class TemperatureBehavior implements StartupListener {
    
    private final FogCommandChannel ch;
    private final AstroPiLEDMatrix sth;
    TemperatureBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);
        sth = new AstroPiLEDMatrix(ch);
    }
    
        

    @Override
    public void startup() {
        sth.test();
    }
}
