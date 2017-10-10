/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.Behavior;
import com.ociweb.iot.grove.six_axis_accelerometer.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class AccelBehavior implements Behavior {
    private final FogCommandChannel ch;
    
    private final SixAxisAccelerometer_Transducer accSensor;
    private final AccerometerValues values;

    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        this.values = new AccerometerValues() {
            @Override
            public void onChange(Changed changed) {
                if (changed == Changed.mag) {
                    System.out.println("heading: " + values.getHeading());
                }
                else if (changed == Changed.accel) {
                    System.out.println("accel x: " + values.getAccelX() + " y: " + values.getAccelY() + " z: "+ values.getAccelZ());
                }
            }
        };
        accSensor = new SixAxisAccelerometer_Transducer(ch, values, values, null);
    }
}
