package com.ociweb.iot.maker;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ociweb.iot.hardware.impl.test.TestHardware;
import com.ociweb.pronghorn.stage.scheduling.NonThreadScheduler;

public class ObjectPassingTest {

	 public class TestPojo implements Serializable {
		 
		 private static final long serialVersionUID = 1L;
		 
		 private String name;
		 public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		private int age;
		 
		 public TestPojo(){
		 }
		 
		 public TestPojo(String name, int age) {
			 this.name = name;
			 this.age = age;
		 }
		 
		 @Override
		 public boolean equals(Object that) {
			 if (that instanceof TestPojo) {				 
				 return this.name.equals(((TestPojo)that).name) && this.age==(((TestPojo)that).age);
			 }
			 return false;
		 }
		 
	 }
	
	 @Test
	    public void testApp()
	    {
		 		 
		 
		    final AtomicInteger count = new AtomicInteger(0);
		    final Serializable serialized1 = new String("hello");
		  //  final Serializable serialized1 = new TestPojo("hello",42);
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
						public void message(CharSequence topic, PayloadReader payload) {
							
							try {
								
								assertEquals(serialized1, payload.readObject());
								assertEquals(serialized2, payload.readObject());
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								fail();
							}
							System.out.println("success:"+count.incrementAndGet());
							
							
							
							cc1.openTopic("test\topic").writeObject(serialized1).writeObject(serialized2).publish();
							
							
						}}).addSubscription("test\topic");
					
					
					final CommandChannel cc2 = runtime.newCommandChannel();
					runtime.addStartupListener(new StartupListener() {

						@Override
						public void startup() {
						
							cc2.openTopic("test\topic").writeObject(serialized1).writeObject(serialized2).publish();
							
						}
						
					});
										
				}
	    		
	    	});
	    	    	
	    	NonThreadScheduler scheduler = (NonThreadScheduler)runtime.getScheduler();    	
	    
	    	scheduler.setSingleStepMode(true);
	    	scheduler.startup();

	    	TestHardware hardware = (TestHardware)runtime.getHardware();
	    
	    	
	    	while (count.get() < 5) {
	    		
	    		scheduler.run();
	    		
	    	}
	    	
	    	
	    }
}
