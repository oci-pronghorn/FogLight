package com.ociweb.grove;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class MP3Player implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////


    @Override
    public void declareConnections(Hardware c) {
    	c.useSerial(Baud.B_____9600);
    	c.setTimerPulseRate(1000);
    	//c.enableTelemetry();
    	c.limitThreads();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	runtime.registerListener(new MP3Behavior(runtime));
    	runtime.registerListener(new MonitoringBehavior());
    }
    public static void main (String[] args){
    	FogRuntime.run(new MP3Player());
    }
          
}
