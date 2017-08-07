/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.grove.six_axis_accelerometer.*;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;

/**
 *
 * @author huydo
 */
public class AccelBehavior implements AccelValsListener,StartupListener,MagValsListener { 
    private final FogCommandChannel ch;
    
    private final SixAxisAccelerometer_Transducer accSensor;
    
    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel();     
        accSensor = new SixAxisAccelerometer_Transducer(ch,this);
    }
   
    @Override
    public void startup() {
        accSensor.setAccelScale(6);
        accSensor.setMagScale(8);
    }

    @Override
    public void accelVals(int x, int y, int z) {
        System.out.println("x: "+x);
        System.out.println("y: "+y);
        System.out.println("z: "+z);
    }

    @Override
    public void magVals(int x, int y, int z) {
        double heading = 180*Math.atan2(y, x)/3.14;
        heading = (heading<0)?(heading+360):heading;
        System.out.println("heading: "+heading);
    }
    
}
