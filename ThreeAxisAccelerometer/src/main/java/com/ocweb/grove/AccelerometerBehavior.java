/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ocweb.grove;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.three_axis_accelerometer_16g.ThreeAxisAccelerometer_16gTwig.*;

import com.ociweb.iot.grove.three_axis_accelerometer_16g.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class AccelerometerBehavior implements StartupListener,AccelInterruptListener,AccelValsListener {
    
    private final FogCommandChannel c;
    private final ThreeAxisAccelerometer_16g_Transducer accSensor;
    
    public AccelerometerBehavior(FogRuntime runtime){
        this.c = runtime.newCommandChannel();
        accSensor = ThreeAxisAccelerometer_16g.GetInterrupt.newTransducer(c);
        accSensor.registerListener(this);
    }
    
    @Override
    public void startup() {
        accSensor.setFreeFallDuration(4);
        accSensor.setFreeFallThreshold(9);
        accSensor.enableFreeFallInterrupt();
    }

    @Override
    public void AccelInterruptStatus(int singletap, int doubletap, int activity, int inactivity, int freefall) {
        if(freefall == 1){
            System.out.println("free falling..");
        }
    }

    @Override
    public void accelerationValues(int x, int y, int z) {
        System.out.println("x: "+x);
        System.out.println("y: "+y);
        System.out.println("z: "+z);
    }


}
