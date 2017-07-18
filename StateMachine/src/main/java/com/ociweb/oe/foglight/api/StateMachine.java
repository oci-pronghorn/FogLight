package com.ociweb.oe.foglight.api;


import static com.ociweb.iot.grove.GroveTwig.*;

import com.ociweb.iot.maker.*;
import com.ociweb.oe.foglight.api.StateMachine.StopLight;

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
    	
    	c.startStateMachineWith(StopLight.Stop);
    	c.setTimerPulseRate(1);
    }

   
	@SuppressWarnings("unchecked")
	@Override
    public void declareBehavior(FogRuntime runtime) {

        
        runtime.addTimePulseListener(new TimingBehavior(runtime));
		runtime.addStateChangeListener(new StateChangeBehavior());
    }
          
}
