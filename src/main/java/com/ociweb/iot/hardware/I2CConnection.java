package com.ociweb.iot.hardware;

public class I2CConnection {

    public final IODevice twig;
    public final byte address;  		//i2c address
    public final byte[] readCmd;  		//bytes sent to device to prompt a read
    public final int readBytes;  		//number of bytes to be read on a read
    public final int register;			//identifier for register you're reading from. Does not have to match device spec.
    public final byte[] setup;			//setup bytes sent to initialize communications
    
    public final int responseMS;
    public final int movingAverageWindowMS;
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.responseMS = twig.response();
        this.movingAverageWindowMS = HardConnection.DEFAULT_AVERAGE;
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.responseMS = responseMS;
        this.movingAverageWindowMS = HardConnection.DEFAULT_AVERAGE;
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS, int movingAverageWindowMS) {
        this.twig = twig;
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.responseMS = responseMS; 
        this.movingAverageWindowMS = movingAverageWindowMS;
    }
    
    @Override
    public String toString() {
        return twig.getClass().getSimpleName()+" "+address;        
    }
}
