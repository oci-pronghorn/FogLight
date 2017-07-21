package com.ociweb.grove;

import com.ociweb.iot.astropi.AstroPiTwig;
import com.ociweb.iot.astropi.AstroPiTwig.AstroPi;
import static com.ociweb.iot.grove.real_time_clock.RTCTwig.*;
import com.ociweb.iot.grove.real_time_clock.*;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.Hardware;
import static com.ociweb.iot.grove.AnalogDigitalTwig.*;

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
//        int reg = 192;
//        byte regadd = (byte)(reg & 0xff);
//        for(int i = reg;i<=255;i++){
//            regadd = (byte)(i & 0xff);
//            AstroPiLEDMatrix2 ob = new AstroPiLEDMatrix2(regadd);
//            c.connect(ob);
//        }

        c.connect(AstroPi.GetJoystick,100);
        //c.setTimerPulseRate(1000);
        //c.connect(RTC.ReadTime);
    }
    
    
    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
        
        runtime.registerListener(new ClockBehavior(runtime));
        
    }
    
}
