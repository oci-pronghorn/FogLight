package com.ociweb.iot.maker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ociweb.gl.api.Builder;
import com.ociweb.gl.api.GreenCommandChannel;
import com.ociweb.gl.api.GreenRuntime;
import com.ociweb.gl.api.PayloadWriter;
import com.ociweb.gl.api.PubSubListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.impl.PayloadReader;
import com.ociweb.gl.impl.schema.MessagePubSub;
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
		 
		    final int features = GreenCommandChannel.DYNAMIC_MESSAGING;
		    
	    	DeviceRuntime runtime = DeviceRuntime.test(new IoTSetup() {

	
				@Override
				public void declareConnections(Hardware builder) {					
				}
				
				@Override
				public void declareBehavior(DeviceRuntime runtime) {
					
					final CommandChannel cc1 = runtime.newCommandChannel(features);
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
								
								e.printStackTrace();
								fail("failed after "+count.get());
							}
							
							Optional<PayloadWriter<MessagePubSub>> pw = cc1.openTopic("test\\topic");
							pw.ifPresent(w -> {
								w.writeObject(serialized1);
								w.writeObject(serialized2);
								w.publish();								
							});
							
							return pw.isPresent();
							
						}}).addSubscription("test\\topic");
					
					
					final CommandChannel cc2 = runtime.newCommandChannel(features);
					runtime.addStartupListener(new StartupListener() {

						@Override
						public void startup() {
						
							Optional<PayloadWriter<MessagePubSub>> pw = cc2.openTopic("test\\topic");
							pw.ifPresent(w -> {

								w.writeObject(serialized1);
								w.writeObject(serialized2);
								w.publish();
								
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
