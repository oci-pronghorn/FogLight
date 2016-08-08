package com.ociweb.iot.hardware;

public class HardwareConnection {

	public final IODevice twig;
	public final int responseMS;
    public final int movingAverageWindowMS;
    public final boolean sendEveryValue;
    public final byte connection;

    public static final int DEFAULT_AVERAGE = 1000;
    
    
	public HardwareConnection(IODevice twig, int connection, int responseMS, int movingAverageWindowMS, boolean sendEveryValue){
		this.twig = twig;
		this.responseMS = responseMS;
		this.movingAverageWindowMS = movingAverageWindowMS;
		this.sendEveryValue = sendEveryValue;
		this.connection = (byte)connection;
	}

    public HardwareConnection(IODevice twig, int connection) {
    	this(twig, connection, twig.response(), HardwareConnection.DEFAULT_AVERAGE, false);
        assert(connection<255 && connection>=0);
    }
    
    public HardwareConnection(IODevice twig, int connection, int customResponse) {
    	this(twig, connection, Math.max(customResponse, twig.response()), HardwareConnection.DEFAULT_AVERAGE, false);
        assert(connection<255 && connection>=0);        
    }
    

	
}
