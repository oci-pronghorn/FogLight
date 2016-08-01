package com.ociweb.iot.hardware;

public class HardConnection {

    public final IODevice twig;
    public final byte connection;
    
    public final int responseMS;
    public final int movingAverageWindowMS;
    
    
    public static final int DEFAULT_AVERAGE = 1000;
    
    public HardConnection(IODevice twig, int connection) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        this.responseMS = twig.response();
        this.movingAverageWindowMS = DEFAULT_AVERAGE;
    }
    
    public HardConnection(IODevice twig, int connection, int customResponse) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        this.responseMS = Math.max(customResponse, twig.response());
        this.movingAverageWindowMS = DEFAULT_AVERAGE;
        
    }
    
    public HardConnection(IODevice twig, int connection, int customResponse, int averageWindowMS) {
        this.twig = twig;
        assert(connection<255 && connection>=0);
        this.connection = (byte)connection;
        this.responseMS = Math.max(customResponse, twig.response());
        this.movingAverageWindowMS = averageWindowMS;
    }
    
}
