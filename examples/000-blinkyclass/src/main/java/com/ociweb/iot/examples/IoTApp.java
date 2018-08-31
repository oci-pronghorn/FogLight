package com.ociweb.iot.examples;

import static com.ociweb.iot.grove.simple_digital.SimpleDigitalTwig.*;
import static com.ociweb.iot.maker.Port.D5;

import java.util.concurrent.atomic.AtomicInteger;

import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;

public class IoTApp implements FogApp {
	   
	public static Port LED_PORT = D5;

    private static final String TOPIC = "light";
    private AtomicInteger count = new AtomicInteger();
	
    public static void main( String[] args) {
        FogRuntime.run(new IoTApp());
    }    
    
    @Override
    public void declareConnections(Hardware c) {
        c.connect(LED, LED_PORT);
    }

    @Override
    public void declareBehavior(FogRuntime runtime) {        
        runtime.registerListener(new BlinkerBehavior(runtime,TOPIC)).addSubscription(TOPIC);
        
        runtime.addPubSubListener((topic,payload)->{
        	count.incrementAndGet();        	
        	return true;
        }).addSubscription(TOPIC);
    }  
    
    public int getBlinkCount() {
    	return count.get();
    }
    
}
