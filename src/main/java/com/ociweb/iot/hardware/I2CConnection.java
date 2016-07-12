package com.ociweb.iot.hardware;

public class I2CConnection {

    public final IODevice twig;
    public final byte address;
    public final byte[] readCmd;
    public final int readBytes;
    public final int register;
    public final byte[] setup;
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
    }
}
