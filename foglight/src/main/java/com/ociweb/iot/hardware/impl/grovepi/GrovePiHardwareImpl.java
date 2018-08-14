package com.ociweb.iot.hardware.impl.grovepi;

import static com.ociweb.iot.grove.simple_analog.SimpleAnalogTwig.UltrasonicRanger;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ociweb.gl.api.Behavior;
import com.ociweb.gl.impl.stage.ReactiveListenerStage;
import com.ociweb.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.ociweb.iot.grove.four_digit_display.Grove_FourDigitDisplay;
import com.ociweb.iot.hardware.ADIODevice;
import com.ociweb.iot.hardware.HardwareImpl;
import com.ociweb.iot.hardware.HardwarePlatformType;
import com.ociweb.iot.hardware.I2CConnection;
import com.ociweb.iot.hardware.IODevice;
import com.ociweb.iot.maker.Baud;
import com.ociweb.iot.maker.Hardware;
import com.ociweb.iot.maker.Port;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.schema.GroveResponseSchema;
import com.ociweb.pronghorn.iot.schema.I2CResponseSchema;
import com.ociweb.pronghorn.pipe.MessageSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.pipe.PipeConfigManager;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GrovePiHardwareImpl extends HardwareImpl {

	private static final Logger logger = LoggerFactory.getLogger(GrovePiHardwareImpl.class);


	private final PiModel model;

	private byte commandIndex = -1;

	//TODO: urgent need for unit tests here, this custom pi logic is easily broken.

	public GrovePiHardwareImpl(GraphManager gm, String[] args, int i2cBus) {
		super(gm, args, i2cBus);

		model = PiModel.detect();
		rs232ClientDevice = model.serialDevice();
		rs232ClientBaud = Baud.B___921600;
		bluetoothDevice = model.bluetoothDevice();
		configI2C = true; 
		System.out.println("You are running on the GrovePi hardware on the "+model);
	}



	@Override
	public MessageSchema schemaMapper(MessageSchema schema) {

		if (schema != GroveResponseSchema.instance) {
			return schema;
		} else {
			return I2CResponseSchema.instance;
		}

	}

	@Override
	public PiCommandChannel newCommandChannel(int features, int instance, PipeConfigManager pcm) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, features, instance, pcm, commandIndex);	
	}

	@Override
	public PiCommandChannel newCommandChannel(int instance, PipeConfigManager pcm) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, 0, instance, pcm, commandIndex);	
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

	//TODO: test override of hasDigitalOrAnalogInputs to false and hasDigitalOrAnalogOutputs to false.

	@Override
	public boolean isListeningToPins(Object listener) {
		return false;//TODO: we have no support for this yet
	}

	@Override
	public boolean isListeningToI2C(Object listener) {
		return super.isListeningToI2C(listener) || super.isListeningToPins(listener);
	}

	@Override
	public <R extends ReactiveListenerStage> R createReactiveListener(GraphManager gm,  Behavior listener, 
			Pipe<?>[] inputPipes, Pipe<?>[] outputPipes, ArrayList<ReactiveManagerPipeConsumer> consumers,
			int parallelInstance, String id) {
		return (R)new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, consumers, this, parallelInstance, id); 
	}

	@Override
	protected Hardware internalConnectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {

		if (t.isInput()) {


			int register = GrovePiConstants.ANALOG_PORT_TO_REGISTER[connection]; 

			//NOTE: may need to add additional "special cases" here
			byte groveOperation = UltrasonicRanger == t ?
					GrovePiConstants.ULTRASONIC_RANGER : 
						GrovePiConstants.ANALOG_READ;


			byte[] readCmd = {GrovePiConstants.START_BYTE,groveOperation,(byte)register,0x00,0x00};
			byte[] setup = {GrovePiConstants.START_BYTE, GrovePiConstants.PIN_MODE, (byte)register,GrovePiConstants.INPUT,0x00};				
			i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,
					GrovePiConstants.BOARD_ADDR,readCmd,(byte)3,register, setup, customRate, customAverageMS, everyValue)); 
			super.internalConnectAnalog(t, register, customRate, customAverageMS, everyValue);


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

	@Override
	public Hardware connect(ADIODevice t, Port port) {
		System.out.println("GrovePiHardware.connect");
		if (t == Grove_FourDigitDisplay.instance){	
			deviceOnPort[port.ordinal()] = t; 
			return connectGroveFirmwareDevice(t,port);
		}
		else {
			return super.connect(t, port);
		}
	}

	/**
	 * This private method exists so that we can make I2C connections for fake-digital-actually-I2C devices we are hiding as digital.
	 * @param t the special IO device that is actually I2C underneath
	 * @param p the port(s) needed (optional)
	 * @return the GrovePiHardware once the proper connections are made
	 */
	private Hardware connectGroveFirmwareDevice(IODevice t, Port... p){
		if (t == Grove_FourDigitDisplay.instance){
			logger.info("connectGroveFirmwareDevice");
			I2CConnection con = Grove_FourDigitDisplay.instance.getI2CConnection();
			con.setup[1] = p[0].port;
			
			i2cOutputs = growI2CConnections(i2cOutputs,  con);
			return this;
			
			
		}
		throw new UnsupportedOperationException("Do not call connect GroveFirmwareDevice unless you are connecting: /n 1) FourDigitDisplay");
	}


}

