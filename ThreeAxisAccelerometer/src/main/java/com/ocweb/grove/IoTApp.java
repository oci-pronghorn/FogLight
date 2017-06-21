package com.ocweb.grove;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;

public class IoTApp implements FogApp
{
    ///////////////////////
    //Connection constants 
    ///////////////////////
    // // by using constants such as these you can easily use the right value to reference where the sensor was plugged in
      
    //private static final Port BUTTON_PORT = D3;
	//private static final Port LED_PORT    = D4;
    //private static final Port RELAY_PORT  = D7;
    //private static final Port LIGHT_SENSOR_PORT= A2;

    @Override
    public void declareConnections(Hardware c) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        
        // // specify each of the connections on the harware, eg which component is plugged into which connection.        
              
        //c.connect(Button, BUTTON_PORT); 
        //c.connect(Relay, RELAY_PORT);         
        //c.connect(LightSensor, LIGHT_SENSOR_PORT); 
        //c.connect(LED, LED_PORT);        
        //c.useI2C();
        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////
    	XYZListener coordListener = new XYZListener() {
			
			@Override
			public void setCoord(int x, int y, int z) {
				// TODO Auto-generated method stub
				
			}
		};
        
    	FogCommandChannel c = runtime.newCommandChannel();
    	
    	final XYZRequester r = new XYZRequester(c);
    	
    	runtime.addStartupListener(()-> {
    		
    		r.request();
    		
    	});
    	
    	
		runtime.addI2CListener(new XYZReader(coordListener) );
    	
    	
    	
    	
    }
        
  
}
