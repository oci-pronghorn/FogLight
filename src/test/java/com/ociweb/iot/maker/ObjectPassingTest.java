package com.ociweb.iot.maker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ociweb.gl.api.PayloadWriter;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.impl.PayloadReader;
import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

public class ObjectPassingTest {

	 
	
	 @Test
	    public void testApp()
	    { 		 
		 
		    final AtomicInteger count = new AtomicInteger(0);
		  //  final Serializable serialized1 = new String("hello");
		    final Serializable serialized1 = new TestPojo("hello",42);
		    final Serializable serialized2 = new String("world");
		 
	    	DeviceRuntime runtime = DeviceRuntime.test(new IoTSetup() {

				@Override
				public void declareConnections(Hardware c) {
					
				}

				@Override
				public void declareBehavior(DeviceRuntime runtime) {
					
					final CommandChannel cc1 = runtime.newCommandChannel();
					runtime.addPubSubListener(new PubSubListener() {

						@Override
						public boolean message(CharSequence topic, PayloadReader payload) {
							
							try {
								
								Object object1 = payload.readObject();
								//System.out.println(object1.getClass().getCanonicalName());
								
								assertEquals(serialized1, object1);
								
								Object object2 = payload.readObject();
								//System.out.println(object2.getClass().getCanonicalName());
								
								assertEquals(serialized2, object2);
								
								count.incrementAndGet();
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								fail("failed after "+count.get());
							}
							
							PayloadWriter pw = cc1.openTopic("test\\topic");
							pw.writeObject(serialized1);
							pw.writeObject(serialized2);
							pw.publish();
							
							return true;
							
						}}).addSubscription("test\\topic");
					
					
					final CommandChannel cc2 = runtime.newCommandChannel();
					runtime.addStartupListener(new StartupListener() {

						@Override
						public void startup() {
						
							PayloadWriter pw = cc2.openTopic("test\\topic");
							pw.writeObject(serialized1);
							pw.writeObject(serialized2);
							pw.publish();
							
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
