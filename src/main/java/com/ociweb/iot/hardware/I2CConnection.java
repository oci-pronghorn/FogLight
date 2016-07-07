package com.ociweb.iot.hardware;

public class I2CConnection {

    public final IODevice twig;
    public final byte address;
    public final byte[] readCmd;
    public final int readBytes;
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        
    }
}
