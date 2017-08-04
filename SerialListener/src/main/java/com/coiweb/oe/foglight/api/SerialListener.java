package com.coiweb.oe.foglight.api;


import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

public class SerialListener implements FogApp
{

	private Appendable builder;
	
    public SerialListener(Appendable builder) {
		this.builder = builder;
	}


	@Override
    public void declareConnections(Hardware c) {
        c.useSerial(Baud.B_____9600); //optional device can be set as the second argument       
        c.setTimerPulseRate(200);
        c.limitThreads();//picks optimal threads based on core detection
  
        c.enableTelemetry();
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
    	
    	runtime.addSerialListener(new SerialListenerBehavior(builder,runtime));
    	
    	runtime.addTimePulseListener(new SerialWriterBehavior(runtime));

    }
          
}
