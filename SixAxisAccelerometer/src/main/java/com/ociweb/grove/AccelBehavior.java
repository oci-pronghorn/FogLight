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
public class AccelBehavior implements Behavior, AccelValsListener {
    private final FogCommandChannel ch;
    
    private final SixAxisAccelerometer_Transducer accSensor;
    private final AccerometerMagValues magValues;

    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        this.magValues = new AccerometerMagValues(
                AccelerometerMagDataRate.hz50,
                AccelerometerMagScale.gauss8,
                AccelerometerMagRes.high) {

            @Override
            public void onChange() {
                System.out.println("heading: " + magValues.getHeading());
            }
        };
        accSensor = new SixAxisAccelerometer_Transducer(ch, this, magValues, null);
    }

    @Override
    public AccelerometerAccelDataRate getAccerometerDataRate() {
        return AccelerometerAccelDataRate.hz50;
    }

    @Override
    public AccelerometerAccelScale getAccerometerScale() {
        return AccelerometerAccelScale.gauss6;
    }

    @Override
    public int getAccerometerAxes() {
        return AccelerometerAccelAxes.all;
    }

    @Override
    public void accelerationValues(int x, int y, int z) {
        System.out.println("accel x: " + x + " y: " + y + " z: "+ z);
    }
}
