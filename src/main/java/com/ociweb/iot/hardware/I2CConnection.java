package com.ociweb.iot.hardware;

public class I2CConnection extends HardwareConnection {

    public final byte address;  		//i2c address
    public final byte[] readCmd;  		//bytes sent to device to prompt a read
    public final int readBytes;  		//number of bytes to be read on a read
    public final int register;			//identifier for register you're reading from. Does not have to match device spec.
    public final byte[] setup;			//setup bytes sent to initialize communications
    public final long delayAfterRequestNS; //delay between read request and i2c.read
    
    
    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
        super(twig, twig.response(), HardConnection.DEFAULT_AVERAGE);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        //this.delayAfterRequestNS = null==readCmd? 20_000_000 : computeMinimum(readCmd.length, 3);
        this.delayAfterRequestNS = 80_000;
    }
    
    private long computeMinimum(int readRequestLen, int responseLen) {
        
        int totalBits = 9 * (2+readRequestLen+responseLen); //plus 2 for addresses
        int oneSecond = 1_000_000_000;
        int i2cSpeed = 100_000;//cycles per second
        int oneCycle = oneSecond/i2cSpeed;
        int totalTime = oneCycle*totalBits;
        System.out.println("Required wait time: "+totalTime+"ns");        
        return totalTime+20_00_000;
    }

    public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int responseMS) {
    	super(twig, responseMS, HardConnection.DEFAULT_AVERAGE);
        this.address = address;
        this.readCmd = readCmd;
        this.readBytes = readBytes;
        this.register = register;
        this.setup = setup;
        //this.delayAfterRequestNS = null==readCmd? 20_000_000 : computeMinimum(readCmd.length, 3);
        this.delayAfterRequestNS = 80_000;
    }
    

    
    @Override
    public String toString() {
        return twig.getClass().getSimpleName()+" "+address;        
    }
}
