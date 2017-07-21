package com.ociweb.iot.maker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import com.ociweb.gl.api.MessageReader;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

public class ObjectPassingTest {
	 
	
	 @Test
	    public void testApp()
	    { 		 
		 
		    final AtomicInteger count = new AtomicInteger(0);
		  //  final Serializable serialized1 = new String("hello");
		    final Serializable serialized1 = new TestPojo("hello",42);
		    final Serializable serialized2 = new String("world");
		 
		    FogRuntime runtime = FogRuntime.test(new FogApp() {
	
				@Override
				public void declareConnections(Hardware builder) {					
				}
				
				@Override
				public void declareBehavior(FogRuntime runtime) {
					
					runtime.addPubSubListener(new PubSubListener() {
						FogCommandChannel cc1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);

						@Override
						public boolean message(CharSequence topic, BlobReader payload) {
							
							try {
								
								Object object1 = payload.readObject();
								//System.out.println(object1.getClass().getCanonicalName());
								
								assertEquals(serialized1, object1);
								
								Object object2 = payload.readObject();
								//System.out.println(object2.getClass().getCanonicalName());
								
								assertEquals(serialized2, object2);
								
								count.incrementAndGet();
								
							} catch (Exception e) {
								
								e.printStackTrace();
								fail("failed after "+count.get());
							}
							
							return cc1.publishTopic("test\\topic", w -> {
								w.writeObject(serialized1);
								w.writeObject(serialized2);							
							});
							
							
						}}).addSubscription("test\\topic");
					
					
					runtime.addStartupListener(new StartupListener() {
						FogCommandChannel cc2 = runtime.newCommandChannel(DYNAMIC_MESSAGING);

						@Override
						public void startup() {
						
							cc2.publishTopic("test\\topic",w -> {

								w.writeObject(serialized1);
								w.writeObject(serialized2);
								
							});							
						}						
					});									
				}


	    		
	    	});
	    	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();
	    	scheduler.startup();
	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    	
	    	while (count.get() < 5) {
	    		
	    		scheduler.run();
	    		
	    	}
	    	
	    	
	    }
}
