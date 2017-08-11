/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ociweb.grove;

import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Port;

/**
 *
 * @author huydo
 */
public class TempSensorBehavior implements AnalogListener {
    public TempSensorBehavior(FogRuntime runtime) {   
    }
    private final int B = 4275;               // B value of the thermistor
    private final int R0 = 100000;            // R0 = 100k
    @Override
    public void analogEvent(Port port, long time, long durationMillis, int average, int value) {
        // TODO Auto-generated method stub
        double R =  (1023.0/value-1.0);
        R = R0*R;
        double temperature = 1.0/(Math.log(R/R0)/B+1/298.15)-273.15; // convert to temperature via datasheet
        System.out.println("The temperature is : "+temperature+" Celsius");
    }
}
