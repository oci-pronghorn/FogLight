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
public class AccelBehavior implements SixAxisAccelerometerListener,StartupListener { 
    FogCommandChannel ch;
    
    SixAxisAccelerometer_Facade accSensor;
    
    AccelBehavior(FogRuntime runtime){
        this.ch = runtime.newCommandChannel(I2C_WRITER);     
        accSensor = new SixAxisAccelerometer_Facade(ch,this);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startup() {
        
    }

    
}
