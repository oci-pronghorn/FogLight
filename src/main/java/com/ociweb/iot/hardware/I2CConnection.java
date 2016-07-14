package com.ociweb.iot.hardware;

public class I2CConnection {

    public final IODevice twig;
    public final byte address;  		//i2c address
    public final byte[] readCmd;  		//bytes sent to device to prompt a read
    public final int readBytes;  		//number of bytes to be read on a read
    public final int register;			//identifier for register you're reading from. Does not have to match device spec.
    public final byte[] setup;			//setup bytes sent to initialize communications
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;

    }
}
