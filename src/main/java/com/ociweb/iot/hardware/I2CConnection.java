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
	public final int readBytesAtStartUp;            // number of bytes to read at start up
	public final long delayAfterRequestNS; //delay between read request and i2c.read

// TODO: this has to become configurable
	private final static int GROVE_PI_MIN_SCAN_DELAY_NS = 80_000;
	
	public I2CConnection(I2CConnection original ,int responseMS){// for connectI2C(device,response_time) method
		this(original.twig, original.address, original.readCmd, original.readBytes, original.register, original.setup, responseMS, HardwareConnection.DEFAULT_AVERAGE_WINDOW_MS, original.sendEveryValue);
	}

	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup) {
		this(twig,address,readCmd,readBytes,register,setup,false);
	}
        
	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, int readBytesAtStartUp, byte[] setup) {
		this(twig,address,readCmd,readBytes,register,setup,false,readBytesAtStartUp);
	}

	public I2CConnection(I2CConnection original, byte[] newSetup){
		this(original.twig, original.address, original.readCmd, original.readBytes, original.register, newSetup, original.responseMS, HardwareConnection.DEFAULT_AVERAGE_WINDOW_MS, original.sendEveryValue);
	}
        
	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, boolean everyValue) {
		super(twig, UNKOWN_REGISTER, twig.defaultPullRateMS(), HardwareConnection.DEFAULT_AVERAGE_WINDOW_MS, everyValue);
		this.address = address;
		this.readCmd = readCmd;
		this.readBytes = readBytes;
		this.register = register;
		this.setup = setup;
		this.delayAfterRequestNS = GROVE_PI_MIN_SCAN_DELAY_NS +twig.pullResponseMinWaitNS();
		this.readBytesAtStartUp = 0;
	}

	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, boolean everyValue,int readBytesAtStartUp) {
		super(twig, UNKOWN_REGISTER, twig.defaultPullRateMS(), HardwareConnection.DEFAULT_AVERAGE_WINDOW_MS, everyValue);
		this.address = address;
		this.readCmd = readCmd;
		this.readBytes = readBytes;
		this.register = register;
		this.setup = setup;
		this.delayAfterRequestNS = GROVE_PI_MIN_SCAN_DELAY_NS +twig.pullResponseMinWaitNS();
		this.readBytesAtStartUp = readBytesAtStartUp;
	}

	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int pullRateMS) {
		this(twig,address,readCmd,readBytes,register,setup,pullRateMS,HardwareConnection.DEFAULT_AVERAGE_WINDOW_MS, false);
	}

	public I2CConnection(IODevice twig, byte address, byte[] readCmd, int readBytes, int register, byte[] setup, int pullRateMS, int customAverageMS, boolean everyValue) {
		super(twig, UNKOWN_REGISTER, pullRateMS, customAverageMS, everyValue);
		this.address = address;
		this.readCmd = readCmd;
		this.readBytes = readBytes;
		this.register = register;
		this.setup = setup;
		this.delayAfterRequestNS = GROVE_PI_MIN_SCAN_DELAY_NS +twig.pullResponseMinWaitNS();
		this.readBytesAtStartUp = 0;
	}

	@Override
	public String toString() {
		return twig.getClass().getSimpleName()+" "+address;        
	}
}
