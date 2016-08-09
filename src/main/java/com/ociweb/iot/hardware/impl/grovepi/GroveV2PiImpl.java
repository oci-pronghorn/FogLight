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
import com.ociweb.iot.maker.RotaryListener;
import com.ociweb.pronghorn.iot.DexterGrovePiReactiveListenerStage;
import com.ociweb.pronghorn.iot.ReactiveListenerStage;
import com.ociweb.pronghorn.iot.i2c.I2CBacking;
import com.ociweb.pronghorn.iot.schema.GroveRequestSchema;
import com.ociweb.pronghorn.iot.schema.I2CCommandSchema;
import com.ociweb.pronghorn.iot.schema.MessagePubSub;
import com.ociweb.pronghorn.iot.schema.TrafficOrderSchema;
import com.ociweb.pronghorn.pipe.Pipe;
import com.ociweb.pronghorn.stage.scheduling.GraphManager;


public class GroveV2PiImpl extends HardwareImpl {

	private static final Logger logger = LoggerFactory.getLogger(GroveV2PiImpl.class);

	private byte commandIndex = -1;


	public GroveV2PiImpl(GraphManager gm, I2CBacking i2cBacking) {
		super(gm, i2cBacking);
	}


	@Override
	public CommandChannel newCommandChannel(Pipe<GroveRequestSchema> pipe, Pipe<I2CCommandSchema> i2cPayloadPipe, Pipe<MessagePubSub> messagePubSub, Pipe<TrafficOrderSchema> orderPipe) {
		this.commandIndex++;
		return new PiCommandChannel(gm, this, pipe, i2cPayloadPipe, messagePubSub, orderPipe, commandIndex);	
	}

	@Override
	public HardwareImpl connectAnalog(IODevice t, int connection) {
		return connectAnalog(t,connection, t.response());
	}

	@Override
	public HardwareImpl connectAnalog(IODevice t, int connection, int customRate) {
	    super.connectAnalog(t, connection, customRate);
		if(t.isGrove()){
			if (t.isInput()) {
				assert(!t.isOutput());
				connection = GrovePiConstants.ANALOG_PIN_TO_REGISTER[connection];
				byte[] temp = {0x01,0x03,(byte)connection,0x00,0x00};
				byte[] setup = {0x01, 0x05, (byte)connection,0x00,0x00}; //TODO: make more readable
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,temp,(byte)3,connection, setup, customRate));
			} else {
				assert(t.isOutput());
				assert(!t.isInput());
				connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
				byte[] setup = {0x01, 0x05, (byte)connection, 0x01, 0x00};
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,(byte)4,null,0,connection, setup, customRate));
			}
		}else{
			throw new UnsupportedOperationException("you have tried to connect an analog device to a GPIO pin");
		}
		return this;
	}
	
	//////////////////////////////////  connectAnalog ...
	////TODO: THESE METHODS ARE A COMPLETE DISASTER
	///        WE NEED TO EXTRACT THE COMMON LOGIC AND NOT FORCE LEAF CLASSES TO IMPLMENT EVERY SINGLE SIGNATURE.
	//////////////////////////////////////
	
	@Override
	public Hardware connectAnalog(IODevice t, int connection, int customRate, int customAverageMS, boolean everyValue) {
		
		super.connectAnalog(t, connection, customRate, customAverageMS, everyValue);
		if(t.isGrove()){
			if (t.isInput()) {
				assert(!t.isOutput());
				connection = GrovePiConstants.ANALOG_PIN_TO_REGISTER[connection];
				byte[] temp = {0x01,0x03,(byte)connection,0x00,0x00};
				byte[] setup = {0x01, 0x05, (byte)connection,0x00,0x00};				
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,temp,(byte)3,connection, setup, customRate, everyValue));
			} else {
				assert(t.isOutput());
				assert(!t.isInput());
				connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
				byte[] setup = {0x01, 0x05, (byte)connection,0x01,0x00};
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,(byte)4,null,0,connection, setup, customRate, everyValue));
			}
		}else{
			throw new UnsupportedOperationException("you have tried to connect an analog device to a GPIO pin");
		}
		return this;
		
		
	}

	@Override
	public HardwareImpl connectDigital(IODevice t, int connection) {
		return connectDigital(t,connection,t.response());
	}

	@Override
	public HardwareImpl connectDigital(IODevice t, int connection, int customRate) { //TODO: add customRate support
	    super.connectDigital(t, connection, customRate);
		if(t.isGrove()){
			
			if (t.getClass()== GroveTwig.RotaryEncoder.getClass()) {
			
				assert(connection==2) : "RotaryEncoders may only be connected to ports 2 and 3 on Pi";
				
				byte[] ENCODER_READCMD = {0x01, 11, 0x00, 0x00, 0x00};
			    byte[] ENCODER_SETUP = {0x01, 16, 0x00, 0x00, 0x00};
			    byte ENCODER_ADDR = 0x04;
			    byte ENCODER_BYTESTOREAD = 2;
			    byte ENCODER_REGISTER = 2;
			    i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t, ENCODER_ADDR, ENCODER_READCMD, ENCODER_BYTESTOREAD, ENCODER_REGISTER, ENCODER_SETUP));

				logger.info("connected Pi encoder");

			} else if (t.isInput()) {
				assert(!t.isOutput());
				connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
				byte[] readCmd = {0x01,0x01,(byte)connection,0x00,0x00};
				byte[] setup = {0x01, 0x05, (byte)connection,0x00,0x00};
				logger.info("Digital Input Connected on "+connection);
				i2cInputs = growI2CConnections(i2cInputs, new I2CConnection(t,(byte)4,readCmd,1,connection,setup,customRate));
			} else {
				assert(t.isOutput());
				assert(!t.isInput());
				connection = GrovePiConstants.DIGITAL_PIN_TO_REGISTER[connection];
				byte[] setup = {0x01, 0x05, (byte)connection,0x01,0x00};
				logger.info("Digital Output Connected on "+connection);
				i2cOutputs = growI2CConnections(i2cOutputs, new I2CConnection(t,(byte)4,null,0,connection,setup,customRate));
			}
		}else{
			System.out.println("GPIO not currently supported");
		}
		return this;
	}  

	public void coldSetup() {
		//usedLines = buildUsedLines();
		//GrovePiGPIO.ensureAllLinuxDevices(usedLines);
	}

	public void cleanup() {
		//GrovePiGPIO.removeAllLinuxDevices(usedLines);
	}

	public void beginPinConfiguration() {
		//super.beginPinConfiguration();        
	}

	public void endPinConfiguration() {
		//super.endPinConfiguration();
	}

	public int digitalRead(int connector) { 
		throw new UnsupportedOperationException();
	}

	//TODO: Since there's no ADC built into the Pi, we can only read HI or LO.
	public int analogRead(int connector) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void analogWrite(int connector, int value) {
		throw new UnsupportedOperationException();
	}

	//Now using the JFFI stage
	public void digitalWrite(int connector, int value) {
		System.out.println("GPIO not currently supported on Pi");
	}


	static void findDup(HardwareConnection[] base, int baseLimit, HardwareConnection[] items, boolean mapAnalogs) {
		int i = items.length;
		while (--i>=0) {
			int j = baseLimit;
			while (--j>=0) {
				//TODO: Will probably have undesired side effects.
				//                if (mapAnalogs ? base[j].connection == GrovePiConstants.ANALOG_CONNECTOR_TO_PIN[items[i].connection] :  base[j]==items[i]) {
				if (mapAnalogs ? false : base[j] == items[i]) {
					throw new UnsupportedOperationException("Connector "+items[i]+" is assigned more than once.");
				}
			}
		}     
	}
	
	@Override
	public boolean hasI2CInputs() {
		return super.hasI2CInputs()|super.hasDigitalOrAnalogInputs();
	}
	
	public HardwareConnection[] buildUsedLines() {

		HardwareConnection[] result = new HardwareConnection[digitalInputs.length+
		                                             digitalOutputs.length+
		                                             pwmOutputs.length+
		                                             analogInputs.length+
		                                             (configI2C?2:0)];

		int pos = 0;
		System.arraycopy(digitalInputs, 0, result, pos, digitalInputs.length);
		pos+=digitalInputs.length;

		findDup(result,pos,digitalOutputs, false);
		System.arraycopy(digitalOutputs, 0, result, pos, digitalOutputs.length);
		pos+=digitalOutputs.length;

		findDup(result,pos,pwmOutputs, false);
		System.arraycopy(pwmOutputs, 0, result, pos, pwmOutputs.length);
		pos+=pwmOutputs.length;

		if (configI2C) {
			findDup(result,pos,GrovePiConstants.i2cPins, false);
			System.arraycopy(GrovePiConstants.i2cPins, 0, result, pos, GrovePiConstants.i2cPins.length);
			pos+=GrovePiConstants.i2cPins.length;
		}

		return result;
	}

	public boolean isListeningToI2C(Object listener) {
		return listener instanceof I2CListener || listener instanceof DigitalListener || listener instanceof AnalogListener || listener instanceof RotaryListener;
	}

	public boolean isListeningToPins(Object listener) {
		return false;//TODO: we have no support for this yet
	}

    public ReactiveListenerStage createReactiveListener(GraphManager gm,  Object listener, Pipe<?>[] inputPipes, Pipe<?>[] outputPipes) {
        return new DexterGrovePiReactiveListenerStage(gm, listener, inputPipes, outputPipes, this); 
    }

}

