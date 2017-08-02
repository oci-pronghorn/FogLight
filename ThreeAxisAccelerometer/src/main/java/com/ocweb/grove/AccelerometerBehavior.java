/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ocweb.grove;

import com.ociweb.gl.api.StartupListener;

import com.ociweb.iot.grove.three_axis_accelerometer_16g.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */
public class AccelerometerBehavior implements StartupListener,AccelInterruptListener,AccelValsListener {
    
    private final FogCommandChannel c;
    private final ThreeAxisAccelerometer_16g_Transducer accSensor;
    
    public AccelerometerBehavior(FogRuntime runtime){
        this.c = runtime.newCommandChannel(I2C_WRITER);
        accSensor = new ThreeAxisAccelerometer_16g_Transducer(c,this);
    }
    
    @Override
    public void startup() {
        accSensor.powerOn();
        accSensor.setRange(4);
        accSensor.setFreeFallDuration(4);
        accSensor.setFreeFallThreshold(9);
        accSensor.enableFreeFallInterrupt();
    }

    @Override
    public void accelVals(int x, int y, int z) {
        System.out.println("z: "+z);
    }

    @Override
    public void AccelInterruptStatus(int singletap, int doubletap, int activity, int inactivity, int freefall) {
        System.out.println("free fall: "+freefall);
    }


}
