package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.impl.schema.MessagePubSub;
import com.ociweb.gl.impl.schema.TrafficOrderSchema;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.HardwarePlatformType;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStageIOT;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.network.schema.ClientHTTPRequestSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfig;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GrovePiHardwareImpl extends HardwareImpl {

	private static final Logger logger = LoggerFactory.getLogger(GrovePiHardwareImpl.class);

	private byte commandIndex = -1;

	//TODO: urgent need for unit tests here, this custom pi logic is easily broken.

	public GrovePiHardwareImpl(GraphManager gm, I2CBacking i2cBacking) {
		super(gm, i2cBacking);
		System.out.println("You are running on the GrovePi hardware.");
	}


	@Override
	public CommandChannel newCommandChannel(int features, int instance, PipeConfigManager pcm) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, features, instance, pcm, commandIndex);	
	}

	@Override
	public HardwarePlatformType getPlatformType() {
		return HardwarePlatformType.GROVE_PI;
	}
    
	@Override
	public int read(Port port) { 
		throw new UnsupportedOperationException("GPIO not yet supported");
	}


	@Override
	public void write(Port port, int value) {
		throw new UnsupportedOperationException("GPIO not yet supported");
	}
	
	@Override
	public boolean hasI2CInputs() {
		if (super.hasDigitalOrAnalogInputs()) {
			assert(super.hasI2CInputs()) : "if pi has d/a inputs then it must also have i2c inputs by definition.";
		}
		return super.hasI2CInputs() | super.hasDigitalOrAnalogInputs();
	}

	@Override
	public boolean isListeningToPins(Object listener) {
		return false;//TODO: we have no support for this yet
	}
	
	@Override
	public boolean isListeningToI2C(Object listener) {
		return listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener;
	}

	@Override
    public <R extends ReactiveListenerStage> R createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return (R)new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, this); 
    }

	@Override
	protected Hardware internalConnectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		
		if (t.isInput()) {
		        
				int i = t.pinsUsed();
				while (--i>=0) {
					int register;
		    		register = GrovePiConstants.ANALOG_PORT_TO_REGISTER[connection+i]; 
	
		    		//NOTE: may need to add additional "special cases" here
					byte groveOperation = GroveTwig.UltrasonicRanger == t ?
							                GrovePiConstants.ULTRASONIC_RANGER : 
							                GrovePiConstants.ANALOG_READ;
					
					
					byte[] readCmd = {GrovePiConstants.START_BYTE,groveOperation,(byte)register,0x00,0x00};
					byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)register,GrovePiConstants.INPUT,0x00};				
					i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,
							     GrovePiConstants.BOARD_ADDR,readCmd,(byte)3,register, setup, customRate, customAverageMS, everyValue)); 
					super.internalConnectAnalog(t, register, customRate, customAverageMS, everyValue);
				}
				
				//logger.debug("added device {} to inputs we now have {}",t, i2cInputs.length);
				
		} else {
			    int register;
  			    register = GrovePiConstants.ANALOG_PORT_TO_REGISTER[connection];

				byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)register,GrovePiConstants.OUTPUT,0x00};				
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,register, setup, customRate, customAverageMS, everyValue));
				super.internalConnectAnalog(t, register, customRate, customAverageMS, everyValue);
		}
		
		return this;
	}

	protected Hardware internalConnectDigital(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			
				connection = GrovePiConstants.DIGITAL_PORT_TO_REGISTER[connection];
				byte[] readCmd = { GrovePiConstants.START_BYTE, GrovePiConstants.DIGITAL_READ, (byte) connection, 0x00,	0x00 };
				byte[] setup = { GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte) connection,	GrovePiConstants.INPUT, 0x00 };
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t, GrovePiConstants.BOARD_ADDR,readCmd, (byte) 1, connection, setup, customRate, customAverageMS, everyValue));	

				//logger.debug("added device {} to inputs we now have {}",t, i2cInputs.length);
		} else {
			
				connection = GrovePiConstants.DIGITAL_PORT_TO_REGISTER[connection];
				byte[] setup = { GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte) connection,	GrovePiConstants.OUTPUT, 0x00 };
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t, GrovePiConstants.BOARD_ADDR, null,	0, connection, setup, customRate, customAverageMS, everyValue)); 

		}
		return super.internalConnectDigital(t, connection, customRate, customAverageMS, everyValue);
	}
	
	public byte convertToPort(byte connection) {
		return (byte)GrovePiConstants.REGISTER_TO_PORT[connection];
	}
	

}

