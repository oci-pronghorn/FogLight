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
    
    private final static int GROVE_PI_MIN_SCAN_DELAY = 80_000;
    
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
    	this(twig,address,readCmd,readBytes,register,setup,false);
    }
    
    public I2CConnection(I2CConnection original, int newRegister){
    	this(original.twig, original.address, original.readCmd, original.readBytes, newRegister, original.setup, original.responseMS, -1, original.sendEveryValue);
    }
    
    
    public I2CConnection(I2CConnection original, byte[] newSetup){
    	this(original.twig, original.address, original.readCmd, original.readBytes, original.register, newSetup, original.responseMS, -1, original.sendEveryValue);
    }
    
    public I2CConnection(I2CConnection original, int responseMs ,byte[] readCmd){
    	this(original.twig, original.address, readCmd, original.readBytes, original.register, original.setup, responseMs, -1, original.sendEveryValue);
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, boolean everyValue) {
        super(twig, -1, twig.response(), HardwareConnection.DEFAULT_AVERAGE, everyValue);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = GROVE_PI_MIN_SCAN_DELAY+twig.scanDelay();
    }
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS) {
    	this(twig,address,readCmd,readBytes,register,setup,responseMS,-1, false);
    }
    
    //TODO: Customer average MS is never used.
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS, int customAverageMS, boolean everyValue) {
    	super(twig, -1, responseMS, HardwareConnection.DEFAULT_AVERAGE, everyValue);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        this.delayAfterRequestNS = GROVE_PI_MIN_SCAN_DELAY+twig.scanDelay();
    }  

    
    @Override
    public String toString() {
        return twig.getClass().getSimpleName()+" "+address;        
    }
}
