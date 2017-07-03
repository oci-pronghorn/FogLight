package com.ociweb.grove;

import static com.ociweb.iot.grove.I2CGroveTwig.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.grove.RTC.*;
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
        c.connectI2C(RTC);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        final FogCommandChannel c = runtime.newCommandChannel();      
        
        RTC clock = new RTC(c);
        
        
        //RTC_Facade clock2 = RTC.newFacade(c);
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
