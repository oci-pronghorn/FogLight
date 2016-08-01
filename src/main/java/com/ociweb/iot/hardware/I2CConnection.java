package com.ociweb.iot.hardware;

public class I2CConnection extends HardwareConnection {

    public final byte address;  		//i2c address
    public final byte[] readCmd;  		//bytes sent to device to prompt a read
    public final int readBytes;  		//number of bytes to be read on a read
    public final int register;			//identifier for register you're reading from. Does not have to match device spec.
    public final byte[] setup;			//setup bytes sent to initialize communications
    public final long delayAfterRequestNS; //delay between read request and i2c.read
    
    public final long DEFAULT_DELAY = 10_000;
    
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
        super(twig, twig.response(), HardConnection.DEFAULT_AVERAGE);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = DEFAULT_DELAY;
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS) {
    	super(twig, responseMS, HardConnection.DEFAULT_AVERAGE);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = DEFAULT_DELAY;
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS, long delayAfterRequestNS) {
    	super(twig, responseMS, HardConnection.DEFAULT_AVERAGE);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = delayAfterRequestNS;
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS, int movingAverageWindowMS, long delayAfterRequestNS) {
    	super(twig, responseMS, movingAverageWindowMS);
    	this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = delayAfterRequestNS;
        
    }
    
    @Override
    public String toString() {
        return twig.getClass().getSimpleName()+" "+address;        
    }
}
