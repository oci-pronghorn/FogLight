package com.ociweb.grove;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class GPS implements FogApp
{
    @Override
    public void declareConnections(Hardware c) {
    	 c.useSerial(Baud.B_____9600);
    	 c.limitThreads();
    	 
    }
    @Override
    public void declareBehavior(FogRuntime runtime) {
       runtime.registerListener(new GPSBehavior(runtime));
    }
          
}
