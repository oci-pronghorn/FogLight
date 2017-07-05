/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ocweb.grove;

import com.ociweb.gl.api.StartupListener;
import static com.ociweb.iot.grove.Accelerometer_16G.Accelerometer_GetXYZ;
import com.ociweb.iot.grove.accelerometer.Accelerometer_16G_Facade;
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
    private final Accelerometer_16G_Facade accSensor;
    
    public AccelerometerBehavior(FogRuntime runtime){
        this.c = runtime.newCommandChannel();
        accSensor = Accelerometer_GetXYZ.newFacade(c);
    }
    
    @Override
    public void startup() {
        accSensor.begin();
        accSensor.setRange(4);
    }
    
    @Override
    public void i2cEvent(int addr, int register, long time, byte[] backing, int position, int length, int mask) {
        System.out.println("backing0: "+(backing[position]));
        System.out.println("backing1: "+(backing[position+1]));
        System.out.println("backing2: "+(backing[position+2]));
        System.out.println("backing3: "+(backing[position+3]));
        System.out.println("backing4: "+(backing[position+4]));
        System.out.println("backing5: "+(backing[position+5]));
        
        
        short[] values = accSensor.intepretData(backing, position, length, mask);
        System.out.println("x= "+values[0]);
        System.out.println("y= "+values[1]);
        System.out.println("z= "+values[2]);
        
    }
    
    
    
}
