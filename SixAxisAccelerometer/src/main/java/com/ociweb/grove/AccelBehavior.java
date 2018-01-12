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
    private final FogCommandChannel channel;
    private final SixAxisAccelerometer_Transducer accSensor;
    private final AccelerometerValues values;

    AccelBehavior(FogRuntime runtime){
        this.channel = runtime.newCommandChannel();
        this.values = new AccelerometerValues() {
            @Override
            public void onChange(Changed changed) {
                System.out.println("heading: " + values.getHeading());
                System.out.println("pitch: " + values.getPitch());
                System.out.println("roll: " + values.getRoll());
                System.out.println("tilt: " + values.getTiltHeading());
                System.out.println("mx: " + values.getMagX());
                System.out.println("my: " + values.getMagY());
                System.out.println("mz: " + values.getMagZ());
                System.out.println("ax: " + values.getAccelX());
                System.out.println("ay: " + values.getAccelY());
                System.out.println("az: " + values.getAccelZ());
                System.out.println("");
            }
        };
        accSensor = new SixAxisAccelerometer_Transducer(channel, values, values, null);
    }
}
