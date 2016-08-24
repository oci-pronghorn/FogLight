package com.ociweb.iot.maker;

public enum TimeTrigger {

	OnTheSecond(     1_000),
	OnTheMinute(    60_000),
	OnThe10Minute( 600_000),
	OnThe15Minute( 900_000),
	OnThe20Minute(1200_000),	
    OnTheHour(    3600_000);	
	
	private final long rate;
    
    TimeTrigger(long rate) {
		this.rate = rate;			
	}
    
    public long getRate() {
    	return rate;
    }
    
}
