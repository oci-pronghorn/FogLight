package com.ocweb.grove;


import static com.ociweb.iot.grove.GroveTwig.*;
import com.ociweb.iot.grove.accelerometer.Grove_Accelerometer;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import static com.ociweb.iot.maker.Port.D4;

public class IoTApp implements FogApp
{
    
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    } 
    @Override
    public void declareConnections(Hardware c) {
        c.useI2C();
        //c.connect(Button,D4);
        c.connectI2C(Grove_Accelerometer.instance);
        
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        final FogCommandChannel c = runtime.newCommandChannel();
        
        runtime.addStartupListener(()->{
            Grove_Accelerometer.begin(c);
        });

        runtime.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
//            System.out.println("addr: "+addr);
            //System.out.println("reg: "+register);
//            System.out.println("backing0: "+(backing[position]));
//            System.out.println("backing1: "+(backing[position+1]));
//            System.out.println("backing2: "+(backing[position+2]));
//            System.out.println("backing3: "+(backing[position+3]));
//            System.out.println("backing4: "+(backing[position+4]));
//            System.out.println("backing5: "+(backing[position+5]));
            
//            System.out.println("position: "+position);
//            System.out.println("length: "+length);
            
            int[] values = Grove_Accelerometer.intepretData(backing, position, length, mask);
            System.out.println("x= "+values[0]);
            System.out.println("y= "+values[1]);
            System.out.println("z= "+values[2]);
        });
    }
}
