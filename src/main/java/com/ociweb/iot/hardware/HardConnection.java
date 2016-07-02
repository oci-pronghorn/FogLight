package com.ociweb.iot.hardware;

public class HardConnection {

	public enum ConnectionType{
		GrovePi, Direct;
	}
    public final IODevice twig;
    public final byte connection;
    public final ConnectionType type;
    
    public HardConnection(IODevice twig, int connection, ConnectionType type) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        this.type = type;
        
    }
}
