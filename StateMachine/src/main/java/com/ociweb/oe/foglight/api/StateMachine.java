package com.ociweb.oe.foglight.api;


import com.ociweb.iot.maker.FogApp;
import com.ociweb.iot.maker.FogRuntime;
import com.ociweb.iot.maker.Hardware;

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
