package com.ociweb.device.grove;

public class GroveConnect {

    public final GroveTwig twig;
    public final byte connection;
    
    
    public GroveConnect(GroveTwig twig, int connection) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
    }
}
