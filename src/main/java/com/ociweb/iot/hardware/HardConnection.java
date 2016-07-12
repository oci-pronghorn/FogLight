package com.ociweb.iot.hardware;

public class HardConnection {

    public final IODevice twig;
    public final byte connection;
    
    public HardConnection(IODevice twig, int connection) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        
    }
}
