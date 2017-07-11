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
import com.ociweb.iot.maker.I2CListener;

/**
 *
 * @author huydo
 */
public class AccelerometerBehavior implements I2CListener,StartupListener {
    
    //private static final Logger logger = LoggerFactory.getLogger(AccelerometerBehavior.class);
    private final FogCommandChannel c;
    private final ThreeAxisAccelerometer_16g_Facade accSensor;
    
    public AccelerometerBehavior(FogRuntime runtime){
        this.c = runtime.newCommandChannel();
        accSensor = new ThreeAxisAccelerometer_16g_Facade(c);
    }
    
    @Override
    public void startup() {
        accSensor.begin();
        accSensor.setRange(4);
        
    }
    
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        
        short[] values = accSensor.interpretData(backing, position, length, mask);
        System.out.println("x= "+values[0]);
        System.out.println("y= "+values[1]);
        System.out.println("z= "+values[2]);
    }
    
    
    
}
