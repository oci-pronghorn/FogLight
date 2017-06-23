package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

import com.ociweb.gl.api.StateChangeListener;

public class StateMachine implements FogApp
{

	static String cGreen = "Green";
	static String cYellow = "Yellow";
	static String cRed = "Red";
	
	public enum StopLight{
		
		Go(cGreen), 
		Caution(cYellow), 
		Stop(cRed);
		
		private String color;
		
		StopLight(String lightColor){
			color = lightColor;
		}
		
		public String getColor(){
			return color;
		}
	}


    @Override
    public void declareConnections(Hardware c) {
    	
    	c.startStateMachineWith(StopLight.Go);
    }

   
    @Override
    public void declareBehavior(FogRuntime runtime) {

        final FogCommandChannel channel1 = runtime.newCommandChannel(DYNAMIC_MESSAGING);
        final FogCommandChannel channel2 = runtime.newCommandChannel(DYNAMIC_MESSAGING);

        
        
        runtime.addStartupListener(()-> {
        	channel2.changeStateTo(StopLight.Caution);
        	System.out.println("first");
        });
        
        
    	StateChangeListener<StopLight> thing = (oldState, newState) -> {
    		
    		if(newState == StopLight.Go){
    			System.out.println("Go! The light is " + StopLight.Go.getColor());
    			if(channel1.changeStateTo(StopLight.Caution)){
    				channel1.block(7000);
    			}
    		}
    		
    		else if(newState == StopLight.Caution){
    			System.out.println("The light is " + StopLight.Caution.getColor());

    			if(channel1.changeStateTo(StopLight.Stop)){
    				channel1.block(7000);
    			}
    		}
    		
    		else{
    			System.out.println("Stop! The light is " + StopLight.Stop.getColor());
    			if(channel1.changeStateTo(StopLight.Go)){
    				channel1.block(7000);
    			}
    		}
    		return true;
    	};
    	
		runtime.addStateChangeListener(thing);
    	
    }
          
}
