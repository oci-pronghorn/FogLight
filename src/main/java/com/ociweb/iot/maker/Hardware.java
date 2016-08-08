package com.ociweb.iot.maker;

import com.ociweb.iot.hardware.IODevice;

public interface Hardware {
	
	 public Hardware connectAnalog(IODevice t, int connection);
	    
	 public Hardware connectAnalog(IODevice t, int connection, int customRate);
	   
	 public Hardware connectAnalog(IODevice t, int connection, int customRate, int customAverageMS);
	    
	 public Hardware connectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue);
	    
	 public Hardware connectDigital(IODevice t, int connection);
	  
	 public Hardware connectDigital(IODevice t, int connection, int customRate);
	  
	 public Hardware connectDigital(IODevice t, int connection, int customRate, int customAverageMS);
	   
	 public Hardware connectDigital(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue);
	   
 	 public Hardware connectI2C(IODevice t);
	
	 public <E extends Enum<E>> Hardware startStateMachineWith(E state);
	    
	 public Hardware setTriggerRate(long rateInMS);
	    
	 public Hardware useI2C();	
	
	
}
