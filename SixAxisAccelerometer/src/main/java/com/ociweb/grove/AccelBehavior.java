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
import static com.ociweb.iot.maker.FogRuntime.*;

/**
 *
 * @author huydo
 */
public class AccelBehavior implements AccelValsListener,StartupListener,MagValsListener { 
    private final FogCommandChannel ch;
    
    private final SixAxisAccelerometer_Facade accSensor;
    
    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);     
        accSensor = new SixAxisAccelerometer_Facade(ch,this,this);
        runtime.registerListener(accSensor);
    }
   
    @Override
    public void accelVals(int x,int y,int z){
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

    @Override
    public void startup() {
        accSensor.begin();
    }

    
}
