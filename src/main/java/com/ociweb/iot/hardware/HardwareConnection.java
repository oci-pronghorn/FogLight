package com.ociweb.iot.hardware;

public class HardwareConnection {

	public final IODevice twig;
	public final int responseMS;
    public final int movingAverageWindowMS;
    public final boolean sendEveryValue;
    public final byte register;

    public static final int DEFAULT_AVERAGE = 1000;
    
    
	public HardwareConnection(IODevice twig, int register, int responseMS, int movingAverageWindowMS, boolean sendEveryValue){
		this.twig = twig;
		this.responseMS = (responseMS==-1)?twig.response():responseMS;
		this.movingAverageWindowMS = (movingAverageWindowMS==-1)?HardwareConnection.DEFAULT_AVERAGE:movingAverageWindowMS;
		this.sendEveryValue = sendEveryValue;
		this.register = (byte)register;
	}

    public HardwareConnection(IODevice twig, int connection) {
    	this(twig, connection, twig.response(), HardwareConnection.DEFAULT_AVERAGE, false);
    }
    
    public HardwareConnection(IODevice twig, int connection, int customResponse) {
    	this(twig, connection, Math.max(customResponse, twig.response()), HardwareConnection.DEFAULT_AVERAGE, false);       
    }
    
	
}
