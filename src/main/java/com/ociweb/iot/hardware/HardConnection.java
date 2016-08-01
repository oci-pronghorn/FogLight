package com.ociweb.iot.hardware;

public class HardConnection extends HardwareConnection {

    public final byte connection;
    
    
    public static final int DEFAULT_AVERAGE = 1000;
    
    public HardConnection(IODevice twig, int connection) {
    	super(twig, twig.response(), HardConnection.DEFAULT_AVERAGE);
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;

    }
    
    public HardConnection(IODevice twig, int connection, int customResponse) {
    	super(twig, Math.max(customResponse, twig.response()), HardConnection.DEFAULT_AVERAGE);
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        
    }
    
    public HardConnection(IODevice twig, int connection, int customResponse, int averageWindowMS) {
    	super(twig, Math.max(customResponse, twig.response()), averageWindowMS);
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
    }
    
}
