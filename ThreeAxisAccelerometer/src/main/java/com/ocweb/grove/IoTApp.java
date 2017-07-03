package com.ocweb.grove;


import static com.ociweb.iot.grove.I2CGroveTwig.*;
import com.ociweb.iot.grove.accelerometer.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class IoTApp implements FogApp
{
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 
    @Override
    public void declareConnections(Hardware c) {
        c.useI2C();
        //c.connect(Button,D4);
        c.connectI2C(ThreeAxis_Accelerometer_16G);
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        final FogCommandChannel c = runtime.newCommandChannel();
        Accelerometer_16g accSensor = new Accelerometer_16g(c);
        runtime.addStartupListener(()->{
            accSensor.begin();
            accSensor.setRange(4);
        });

        runtime.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
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
        });
    }
}
