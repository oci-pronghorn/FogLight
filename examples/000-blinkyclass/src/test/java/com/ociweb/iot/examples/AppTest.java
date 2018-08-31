package com.ociweb.iot.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.iot.maker.FogRuntime;

/**
 * Unit test for simple App.
 */
public class AppTest { 

  @Test
    public void testApp() {
	  	  
	   IoTApp app = new IoTApp();
	   FogRuntime.testConcurrentUntilShutdownRequested(app, 2_000) ;
	   assertTrue(app.getBlinkCount()>50);
	   assertTrue(app.getBlinkCount()<300);
	      	
    }
    
}
