package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.IODevice;

public interface Hardware {
	
	 public Hardware connect(IODevice t, Port port);
	 public Hardware connect(IODevice t, Port port, int customRateMS);
	 public Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWinMS);
	 public Hardware connect(IODevice t, Port port, int customRateMS, int customAvgWinMS, boolean everyValue);
	 	 	   
 	 public Hardware connectI2C(IODevice t);
	
	 public <E extends Enum<E>> Hardware startStateMachineWith(E state);
	    
	 public Hardware setTriggerRate(long rateInMS);
	 public Hardware setTriggerRate(TimeTrigger trigger);
	 
	    
	 public Hardware useI2C();	
		
}
