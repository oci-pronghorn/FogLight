package com.ociweb.device;

public class Connect {

    public final GroveTwig twig;
    public final byte connection;
    
    public Connect(GroveTwig twig, int connection) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
    }
}
