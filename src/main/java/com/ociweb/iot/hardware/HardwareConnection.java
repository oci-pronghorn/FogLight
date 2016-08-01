package com.ociweb.iot.hardware;

public abstract class HardwareConnection {

	public IODevice twig;
	public final int responseMS;
    public final int movingAverageWindowMS;
    
	public HardwareConnection(IODevice twig, int responseMS, int movingAverageWindowMS){
		this.twig = twig;
		this.responseMS = responseMS;
		this.movingAverageWindowMS = movingAverageWindowMS;
	}

}
