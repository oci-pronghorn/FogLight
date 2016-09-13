package com.ociweb.iot.hardware.impl.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.iot.grove.GroveTwig;
import com.ociweb.iot.hardware.HardwareConnection;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.AnalogListener;
import com.ociweb.iot.maker.CommandChannel;
import com.ociweb.iot.maker.DigitalListener;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.I2CListener;
import com.ociweb.iot.maker.Port;
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.NetRequestSchema;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
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
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<NetRequestSchema> httpRequest, Pipe<TrafficOrderSchema> orderPipe) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, pipe, i2cPayloadPipe, messagePubSub, httpRequest, orderPipe, commandIndex);	
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
    public ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, this); 
    }

	@Override
	protected Hardware internalConnectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
		
	    		connection = GrovePiConstants.ANALOG_PORT_TO_REGISTER[connection];
				byte[] readCmd = {GrovePiConstants.START_BYTE,GrovePiConstants.ANALOG_READ,(byte)connection,0x00,0x00};
				byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.INPUT,0x00};				
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,readCmd,(byte)3,connection, setup, customRate, customAverageMS, everyValue)); 
			
		} else {

	    		connection = GrovePiConstants.ANALOG_PORT_TO_REGISTER[connection];
				byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)connection,GrovePiConstants.OUTPUT,0x00};				
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,GrovePiConstants.BOARD_ADDR,null,0,connection, setup, customRate, customAverageMS, everyValue));

		}
		return super.internalConnectAnalog(t, connection, customRate, customAverageMS, everyValue);
	}

	protected Hardware internalConnectDigital(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		if (t.isInput()) {
			
				connection = GrovePiConstants.DIGITAL_PORT_TO_REGISTER[connection];
				byte[] readCmd = { GrovePiConstants.START_BYTE, GrovePiConstants.DIGITAL_READ, (byte) connection, 0x00,	0x00 };
				byte[] setup = { GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte) connection,	GrovePiConstants.INPUT, 0x00 };
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t, GrovePiConstants.BOARD_ADDR,readCmd, (byte) 1, connection, setup, customRate, customAverageMS, everyValue));	

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

