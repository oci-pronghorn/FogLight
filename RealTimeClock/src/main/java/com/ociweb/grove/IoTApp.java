package com.ociweb.grove;

import com.ociweb.iot.grove.real_time_clock.*;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.Hardware;


public class IoTApp implements FogApp
{
    
    public static void main( String[] args ) {
        FogRuntime.run(new IoTApp());
    }
    
    
    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.
        c.useI2C();
        c.connect(RTCTwig.ReadTime);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        final FogCommandChannel c = runtime.newCommandChannel();      
        
        RTC_Facade clock = new RTC_Facade(c);
        
        runtime.addStartupListener(()->{
//            clock.startClock();
//            clock.setTime(0, 50, 13, 3, 28, 6, 17);
            
        });

        runtime.addI2CListener((int addr, int register, long time, byte[] backing, int position, int length, int mask)->{
            
            int[] temp = clock.intepretData(backing, position, length, mask);
            clock.printTime(temp);
                    
        });
        

    }
    
}
