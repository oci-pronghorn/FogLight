package com.ociweb.iot.hardware;

/**
 * Contains all the information necessary for reading specific I2C Devices. Is used by I2CJFFIStage.
 * @author alexherriott
 *
 */
public class I2CConnection extends HardwareConnection {

    public final byte address;  		//i2c address
    public final byte[] readCmd;  		//bytes sent to device to prompt a read
    public final int readBytes;  		//number of bytes to be read on a read
    public final int register;			//identifier for register you're reading from. Does not have to match device spec.
    public final byte[] setup;			//setup bytes sent to initialize communications
    public final long delayAfterRequestNS; //delay between read request and i2c.read
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
    	this(twig,address,readCmd,readBytes,register,setup,false);
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, boolean everyValue) {
        super(twig, -1, twig.response(), HardwareConnection.DEFAULT_AVERAGE, everyValue);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = 80_000; //TODO: + additional delay known by twig
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS) {
    	this(twig,address,readCmd,readBytes,register,setup,responseMS,-1, false);
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS, int customAverageMS, boolean everyValue) {
    	super(twig, -1, responseMS, HardwareConnection.DEFAULT_AVERAGE, everyValue);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = 80_000; //TODO: + additional delay known by twig
    }  

    
    @Override
    public String toString() {
        return twig.getClass().getSimpleName()+" "+address;        
    }
}
