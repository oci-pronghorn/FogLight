/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.grove.six_axis_accelerometer.SixAxisAccelerometer_Facade;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class AccelBehavior implements I2CListener,StartupListener {
    FogCommandChannel ch;
    SixAxisAccelerometer_Facade accSensor;
    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();
        accSensor = new SixAxisAccelerometer_Facade(ch);
    }
    @Override
    public void startup() {
        accSensor.begin();
    }
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        short[] values = accSensor.interpretXYZ(backing, position, length, mask);
        System.out.println("x: "+values[0]);
        System.out.println("y: "+values[1]);
        System.out.println("z: "+values[2]);
    }
    
}
